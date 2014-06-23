
# Format

- "Ignite" style talk
- 5m total time 
	- 120 words per minute
	- 1 slide per paragraph
	- 15 seconds per slide
	- 20 slides total

## Attributions


- 2 https://www.flickr.com/photos/84568447@N00/5325139336/in/photostream/

## 0m

Hello everyone, My name is Etienne Caron.

Today I'll be introducing you to Renderscript, and it's potential uses in your Android application. Renderscript is a framework that allows you to execute computationally intensive tasks on Android. 

It's primarily designed for data-parallel computation. People new to programming often ask me "what does this even mean?" And I like to refer to...

This really great little video, from a live demo the MythBusters did at an NVidia event in 2008. If you think of each paintball pellet as a thread running a small script...

And assuming each of the **scripts** is capable of **rendering** one pixel independently, you'll be leveraging the multiple DSP and GPU cores available on the user's device. 

## 1m

Here people sometime look at me quizically and say "DSP **and** GPU cores?" Why yes! Exactly! Renderscript, unlike other proprietary approaches, will leverage the hardware available on that **specific** device.

"I to learn C, right?" No! I swear! By using ScriptIntrinsic implementations, it's possible to bypass all that scary C code. ScriptIntrinsic are efficient implementations of common image processing operations.

At time of writing, we can pick from 9 different Intrinsic implementations. For our example, let's blur a bitmap. First we need a renderscript context. We instantiate our intrinsic, then ...

We need an empty bitmap of identical size and configuration. Allocations here are built based on these bitmaps. Allocations are used to pass data to and from a renscript kernel. (more later..)

## 2m

And we can see the result here. Nice. I hear some of you saying (softly) "But *I* like C, what else could this do?" Well, you can program Renderscript kernels.

Kernels are written a C99-derived language. They are usually stored in a `.rs` file under `<project>/src/main/rs`. You can have multiple kernels per `.rs` script.

Kernels are very similar to standard C functions. A key point is they run ***in parallel*** across every `Element` within an `Allocation`. A kernel can have an input and/or an output `Allocation`. 

Here's an example, the setup is nearly identical as the previous example, and note how we pass parameters to control the vignette effect. These are based off the C99 code.

## 3m

Photos processing, nice. What else? Ultimately, the key to finding interesting uses for Renderscript is to come up with problems that can be solved in parallel. Conway's Game of Life is a good example.

In the Game of Life, you start with a grid. Each cells is "alive" or "dead". A cell is born, lives or dies depending on the amount of immediate neighbors. 

This kind of problem lends itself wonderfully to parallel processing. The programs in each cell depends on the **current state of the system**, and can thus resolve simultaneoulsy.

Procedural content generation and physics simulation lend themselves very nicely to parallel processing. Some of today's most popular games leverage techniques outlined above with great effect. (Fluid dynamics, fire propagation, monster ecology, etc)

## 4m

When I first heard about 'Metal' for iOS, and the supposed performance gained from dropping the OpenGL pipeline, and going  straight to GPUs...

I wasn't actually so surprised. You're looking here at 'raw' GPU rendering. These effects are accomplished by using **shader programs only**, pretty much bypassing the OpenGL pipeline. Functionally similar to using Renderscript Kernels.

If you look at the source code on the right hand side, this specific example is only (see slide) lines long. Here we see the output of a *terrain raymarching* algorithm. The examples here are mostly OpenGL shaders, but with very minor modifications can be executed in a Renderscript context. 

This is all for me. If you're interested to learn more, quick, follow this QR-Code! You'll land on a github project with the slides and a simple starter Renderscript project. You'll also find links to most of what was outlined above. 

Thank you.

## 5m
