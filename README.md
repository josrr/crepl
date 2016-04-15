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

## Screenshots

![Screenshot](https://github.com/josrr/crepl/raw/master/screenshot-color.png)

![Screenshot](https://github.com/josrr/crepl/raw/master/screenshot.png)

## Dependencies

### Embedded Common Lisp (ECL)

For compile and run this application you need the develop branch of ECL that
you can get from https://gitlab.com/embeddable-common-lisp/ecl

Get the develop branch; use this git command line:

    git clone -b develop https://gitlab.com/embeddable-common-lisp/ecl.git

For instructions on how to compile ECL please read [The ECL Quarterly volume 3](https://common-lisp.net/project/ecl/quarterly/volume3.html#orgheadline13).

In Debian or Ubuntu you need the package gcc-multilib.

### Android SDK and NDK

### Gradle

https://gradle.org/

## Compilation of CREPL

After compiling the host and target ECL as said by The ECL Quaterly get the
CREPL repository an go to the cl-android directory to create two symbolic
links:

    git clone https://github.com/josrr/crepl
    cd crepl/cl-android
    ln -s $PATH_TO_ECL_SOURCE/ecl-android-target ecl-android
    ln -s ecl-android/lib/ecl-*.*.* ecl-libdir

Then change to the CREPL main directory and run gradle

    cd ..
    gradle assembleDebug

If everything is OK you can find the APK archive here:

    cl-android/build/outputs/apk/cl-android-debug.apk
