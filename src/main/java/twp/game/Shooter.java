package twp.game;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import com.fasterxml.jackson.annotation.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.type.*;
import twp.*;
import twp.tools.*;

import java.util.*;

public class Shooter {
    public HashMap<UnitType, HashMap<Item, Weapon>> weapons = new HashMap<>();

    public Seq<Data>
        units = new Seq<>(),
        players = new Seq<>(),
        garbage = new Seq<>();

    public Shooter() {
        Logging.on(EventType.UnitCreateEvent.class, e -> {
            units.add(garbage.pop(() -> new Data(e.unit)));
        });

        Logging.on(EventType.PlayerJoin.class, e -> {
            players.add(garbage.pop(() -> new Data(e.player)));
        });

        Boolf<Data> update = d -> {
            Unit u = d.unit;

            // reload cycle
            d.reloadProgress += Time.delta/60;

            // removing garbage except player spawns
            if(u.dead && !u.spawnedByCore()) {
                garbage.add(d);
                return false;
            }

            if(!u.isShooting) {
                return true;
            }

            HashMap<Item, Weapon> inner = weapons.get(u.type);
            if(inner == null) {
                return true;
            }

            Weapon w = inner.get(u.item());
            if(w == null) {
                return true;
            }

            w.shoot(d);
            return true;
        };

        Logging.run(Trigger.update, () -> {
            units.filter(update);

            players.filter(d -> {
                d.unit = d.player.unit();

                if(d.unit == null) {
                    return true;
                }

                if(!d.player.con.isConnected()) {
                    garbage.add(d);
                    return false;
                }

                if(d.unit.spawnedByCore()) {
                    return update.get(d);
                }

                return true;
            });
        });

        load();
    }

    public void load(){
        try{
            Config cfg = Json.loadJackson(Global.weapons, Config.class);
            if(cfg == null) {
                return;
            }
            weapons = cfg.parse();
        } catch(NoSuchFieldException e) {
            Log.info("failed to load weapons: " + e.getMessage());
        }
    }

    public static class Config {
        public HashMap<String, Stats> def = new HashMap<String, Stats>(){{
            put("copperGun", new Stats());
        }};

        public HashMap<String, HashMap<String, String>> links = new HashMap<String, HashMap<String, String>>(){{
            put("alpha", new HashMap<String, String>(){{
                put("copper", "copperGun");
            }});
            put("beta", new HashMap<String, String>(){{
                put("copper", "copperGun");
            }});
            put("gamma", new HashMap<String, String>(){{
                put("copper", "copperGun");
            }});
        }};

        @JsonIgnore
        public HashMap<UnitType, HashMap<Item, Weapon>> parse() throws NoSuchFieldException{
            HashMap<UnitType, HashMap<Item, Weapon>> res = new HashMap<>();
            for(String unit : links.keySet()) {
                UnitType ut;
                try{
                    ut = (UnitType)UnitTypes.class.getField(unit).get(null);
                }catch(IllegalAccessException e){
                    throw Text.formatInvalidField("unit", unit, "units");
                }catch(NoSuchFieldException e){
                    throw new RuntimeException(e);
                }
                HashMap<Item, Weapon> inner = res.computeIfAbsent(ut, k -> new HashMap<>());
                for(String item : links.get(unit).keySet()) {
                    Item i;
                    try{
                        i = (Item)Items.class.getField(item).get(null);
                    }catch(IllegalAccessException e){
                        throw Text.formatInvalidField("item", item, "items");
                    }catch(NoSuchFieldException e){
                        throw new RuntimeException(e);
                    }
                    String defName = links.get(unit).get(item);
                    Stats stats = def.get(defName);
                    if(stats == null) {
                        throw new NoSuchFieldException(String.format("%s does not exist in weapon definitions", defName));
                    }
                    inner.put(i, new Weapon(stats, ut));
                }
            }
            return res;
        }

    }

    public static class Data {
        Unit unit;
        Player player;
        float reloadProgress;
        int ammo;

        public Data(Unit u) {
            this.unit = u;
        }

        public Data(Player p) {
            this.player = p;
        }
    }

    public static class Stats {
        public String
            bullet = "standardCopper"; //bullet type
        public float
            inaccuracy = 2f, // in degrees
            damageMultiplier = 1f,
            reload = .3f; // in seconds
        public int
            bulletsPerShot = 2,
            multiplier = 4,
            itemsPerScoop = 1;
    }

    public static class Weapon {
        public BulletType bullet, original;
        public Stats stats;

        // helper vectors to reduce allocations.
        static Vec2 h1 = new Vec2(), h2 = new Vec2(), h3 = new Vec2();

        public Weapon(Stats stats, UnitType ut) throws NoSuchFieldException {
            this.stats = stats;

            try{
                this.bullet = (BulletType) Bullets.class.getField(stats.bullet).get(null);
            } catch(NoSuchFieldException ex) {
                throw Text.formatInvalidField("bullet", stats.bullet, "bullets");
            } catch(IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            // finding bullet with biggest range
            ut.weapons.forEach(w -> {
                if(original == null || original.range() < w.bullet.range()) {
                    original = w.bullet;
                }
            });
        }

        public void shoot(Data d){
            Unit u = d.unit;
            if(d.reloadProgress < stats.reload) {
                return;
            }
            d.reloadProgress = 0;

            // refilling ammo
            if(d.ammo == 0) {
                // not enough items to get new ammo
                if(u.stack.amount < stats.itemsPerScoop) {
                    return;
                }

                u.stack.amount -= stats.itemsPerScoop;
                d.ammo += stats.multiplier;
            }

            d.ammo--;

            h1
            .set(original.range(), 0) // set length to range
            .rotate(h2.set(u.aimX, u.aimY).sub(u.x, u.y).angle()) // rotate to shooting direction
            .add(h3.set(u.vel).scl(60f * Time.delta)); // add velocity offset

            // its math
            float vel = h1.len() / original.lifetime / bullet.speed;
            float life = original.lifetime / bullet.lifetime;
            float dir = h1.angle();

            if(!bullet.collides) {
                // h2 is already in state of vector from u.pos to u.aim and we only care about length
                life = h2.len() / bullet.range(); // bullet is controlled by cursor
            }

            for(int i = 0; i < stats.bulletsPerShot; i++){
                Call.createBullet(
                    bullet,
                    u.team,
                    u.x, u.y,
                    dir + Mathf.range(-stats.inaccuracy, stats.inaccuracy), // apply inaccuracy
                    stats.damageMultiplier * bullet.damage,
                    vel,
                    life
                );
            }

        }
    }
}
