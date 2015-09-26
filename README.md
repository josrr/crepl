# CREPL: Common Lisp REPL for Android

This program reuses code from the
[Terminal Emulator for Android](https://github.com/jackpal/Android-Terminal-Emulator)
application.

CREPL is licensed under the version 3 of the GPL and Terminal Emulator for
Android under the Apache License Version 2.0;
[as far as I know](http://www.apache.org/licenses/GPL-compatibility.html) is
permited to include code licensed under the Apache License Version 2.0 in a
project licensed under the GPL version 3. More information:
http://www.gnu.org/licenses/license-list.html#GPLCompatibleLicenses.

## Screenshot

![Screenshot](https://github.com/josrr/crepl/raw/master/screenshot.png)

## Dependencies

### Embedded Common Lisp (ECL)

For compile and run this application you need the experimental branch of ECL that
you can get from https://gitlab.com/embeddable-common-lisp/ecl

Get only the experimental branch; use this git command line:

    git clone -b experimental https://gitlab.com/embeddable-common-lisp/ecl.git

For instructions on how to compile ECL please read
https://gitlab.com/embeddable-common-lisp/ecl/blob/experimental/README.android

In Debian or Ubuntu you need the package gcc-multilib.

### Android SDK and NDK

### Gradle

https://gradle.org/

## Compilation of CREPL

Copy the contents of this repository to a directory below the `contrib/`
directory of the experimental branch of ECL:

    git clone https://github.com/josrr/crepl.git
    cp -a crepl ecl/contrib

Then run gradle

    cd ecl/contrib/crepl
    gradle assembleDebug

If everything is OK you can find the APK archive here:

    cl-android/build/outputs/apk/cl-android-debug.apk


