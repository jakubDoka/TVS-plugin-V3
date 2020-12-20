package twp.commands;

import arc.util.*;



import twp.*;
import twp.database.*;
import twp.tools.*;
import java.io.*;
import java.util.*;
import static twp.Main.*;

public class Tester extends Command{
    static final String testFile = Global.config_dir + "test";
    HashMap<String, Test> tests = new HashMap<>();
    public HashMap<String, Long> recent = new HashMap<>();

    public Tester(){
        name = "test";
        argStruct = "[answer-number]";
        description = "Easiest way to get verified.";
    }

    @Override
    public void run(String id, String... args){
        if(cannotInteract(id)){
            return;
        }

        long since = Time.timeSinceMillis(recent.getOrDefault(id, 0L));
        if(since < Global.config.testPenalty){
            setArg(Text.milsToTime(Global.config.testPenalty - since));
            result = Result.penalty;
            return;
        }

        if(args.length > 0 && !tests.containsKey(id)){
            result = Result.wrongOption;
            return;
        }

        Test test = tests.computeIfAbsent(id, k -> new Test(caller, this));

        if(args.length > 0){
            if(isNotInteger(args, 0)){
                return;
            }

            test.processAnswer(Integer.parseInt(args[0]));

            if (result == Result.invalidRequest) {
                return;
            }

            if(test.finished()){
                test.evaluate(caller);
                return;
            }
        }

        test.ask(caller);
    }

    public static class Test{
        Tester tester;
        String question;
        String[] options;
        int progress;
        int points;
        boolean empty;
        HashMap<String, String[]> questions;

        public Test(PD pd, Tester tester){
            this.tester = tester;
            questions = loadQuestions(pd.locString);
            if(questions.isEmpty()){
                pd.sendServerMessage("test-missing");
                tester.tests.remove(pd.player.uuid);
                empty = true;
            }

            pd.sendServerMessage("test-start");
        }

        public HashMap<String, String[]> loadQuestions(String locStr){
            String bundle = testFile + "_" + locStr.replace("-", "_") + ".json";
            File fi = new File(bundle);
            if(!fi.exists() || fi.isDirectory()) bundle = testFile + ".json";
            return Json.loadHashmap(bundle, String[].class, example);
        }

        public void ask(PD pd){
            StringBuilder sb = new StringBuilder();

            question = (String)questions.keySet().toArray()[progress];
            sb.append(question).append("\n");

            options = questions.get(question);
            for(int i = 0; i < options.length; i++){
                sb.append("[yellow]").append(i + 1).append(")[gray]");
                sb.append(options[i].replace("#", ""));
                sb.append("\n");
            }

            pd.sendMessage(sb.toString());
            tester.setArg(1, options.length);
            tester.result = Result.hint;
        }

        public void evaluate(PD pd){
            if(points == questions.size()){
                tester.setArg(ranks.verified.getSuffix());

                RankSetter.terminal.run("", String.valueOf(pd.id), "verified");
            }else{
                tester.setArg(points, questions.size());
                tester.result = Result.testFail;

                tester.recent.put(pd.player.uuid, Time.millis());
            }

            tester.tests.remove(pd.player.uuid);
        }

        public boolean finished(){
            return progress == questions.size();
        }

        public void processAnswer(int answer){
            if(answer > options.length || answer < 0){
                tester.result = Result.invalidRequest;
                tester.setArg(answer, options.length);
                return;
            }

            if(options[answer-1].startsWith("#")){
                points += 1;
            }
            progress++;
        }
    }

    private static final HashMap<String, String[]> example = new HashMap<String, String[]>(){{
        put("Some question?", new String[]{
        "Some answer",
        "Som other answer",
        "#correct answer",
        });
        put("Another question?", new String[]{
        "Some answer",
        "Som other answer",
        "#correct answer",
        "#correct answer",
        });
    }};

    public static Tester game = new Tester();
}
