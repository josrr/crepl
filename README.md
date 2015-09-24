# CREPL
**Common Lisp REPL for Android**

## Dependencies

### Embedded Common Lisp (ECL)

For compile an run this application you need the experimental branch of ECL that
you can get from

https://gitlab.com/embeddable-common-lisp/ecl

To get only the experimental branch use this git command line:

    git clone -b experimental https://gitlab.com/embeddable-common-lisp/ecl.git

For instructions on how to compile ECL please read  https://gitlab.com/embeddable-common-lisp/ecl/blob/experimental/README.android

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


