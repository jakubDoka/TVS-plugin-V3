# TWS plugin

## Contribution

I would really welcome some lively contributor that would push plugin forward because I am burn-out. 
You can improve anything you want as long as your code is maintainable and straight forward. My motto is 
`newer write things twice` so be ready for obscure java abstraction.

## Command structure

My approach to implementing commands is little different to usual, witch is the most basic use of 
command handler in a gigantic init function. I have a decent amount of experience with writing plugins and this
approach is not manageable on TWS scale. When creating command you have to make it its own class that extends 
`twp.command.Command`. There is no other way you should do this unless the command is used only from terminal
and is very short. Now you can dive deep into disgusting code located in command class or just leave it be and 
read the remaining part. 

### concurrency

When you override abstract function `run` keep in mind that all logic is on a separate thread. When 
you are manipulating with game state you have to use `Main.queue` to push it to main thread. Interaction with 
database ,anything under `Main.db`, can be executed concurrently as mongo db is ok with that and there is a lot 
of  staff too. If you have suspicions that code can cause data-races fix or report it immediately as i 
could forget about something (java is messy). 

### resolving

If you look into my command logic you can see I am using numerous helpers to abstract common steps (use them too)
but mainly syntax `result = Result.something` is very frequent. That how you determinate what happened, result will be used as
part of a bundle key that is then passed to caller. Caller (that's a Command property) will then send bundle value to 
player. bundle message can also be parametrized, you can call `setArgs()` and pass all parameters before returning from
`run`.

### registration

Command can be registered on three places, game, terminal and discord. You have the methods that will do it for you, just 
identify witch to use by name (they all named like register(Gm=game, Tm=terminal, Ds=discord)). For each place create 
a static field in your command class and call it discord/terminal/game, then register them accordingly. All methods take handler
and command runner. By default, caller is just notified about command result. If you look into main you will spot one 
command that overrides it and optionally kicks player by providing the command runner. To specify command structure like 
name arguments and description use constructor. 

### testing

Very important part is testing, newer omit this part, look into tests package how testing is done. Every command has its
test class named like `<CommanClassName>Test`. All you have to do is simulate all inputs and assert results with command
method `assertResult` (assert all possible result). This method will panic if result does not match and show you what was 
the wrong result. It will also print message that user will see as a response (make sure to add all bundle keys, it will 
tell what key is missing).

### event registration

When you want to use event, use `Logging.on` and `Logging.run` as they have error handling. Everything has to be under try
catch block, server should newer crash.

### error handling

All code has to be indirectly under try catch. As I stated ine event registration you have to use `Logging` methods to 
save all unexpected errors though before writing garbage everywhere check if error is not already logged as I do not
tolerate redundant code.

### comments

Now, I am not consistant at this front at all, but at least, you should, if you have energy. You can also force me to comment
things by making pr that adds comments to code you don't understand which I will have to fix and add comment eventually.  

### ide

Use Intelij, it's out of question. I refuse to help you with issues related to different ide.

### setup

for plugin to even run you have to install mongoDB on your machine. Now I assume you are programmer, so you will figure 
that out. If not, I can help you (probably).

### summary

Plugin code is tough reading as I optimize for size and avoid verbosity in my code, but you have to understand it and 
learn from it if you want to contribute. There is the amount of things I omitted as this would become the book otherwise.
Be prepared that if I don't like your code, whether it is off the project standards, or your logic is horrible, but idea 
is good (considerd worth my time) I will refactor your code and accept it. What you have to do though is learn from what 
i ve done. 