# Contributing

As long as your pull request doesn't break the app in any way or conflict with an ongoing change (check the open issues first or send me [an email](mailto:dev@jfenn.me) to make sure this doesn't happen), it will likely be accepted. I have a lot of projects and may not get around to reviewing pull requests right away. If you have contributed to this project before and would like to be added to this repository as a collaborator, feel free to send me [an email](mailto:dev@jfenn.me) and ask.

## Branches

I am using the `develop` branch for all changes that won't be immediately available in a new release, with the exception of the README and similar files (like this one). Most PRs should be made to the `develop` branch. The only reason that a PR should be made to `master` is when there is a typo/inconsistency in the README or something similar. If you make a PR to `master` that changes something other than a .md file, I will stare at my screen and make dying lizard noises for approximately 10 minutes before politely asking you to change the branch. Thank you.

## Build Instructions

Importing and building this app in Android Studio should work the same as any other project. If you run into problems, take a look at the [Travis CI](https://travis-ci.com/fennifith/Status) configuration - if Travis can build the project, then you probably can too.

To build the project from the command line, see [Build Your App from the Command Line](https://developer.android.com/studio/build/building-cmdline) (android docs).
