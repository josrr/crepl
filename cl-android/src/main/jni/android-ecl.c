/*
 *  Copyright 2015-2016 José Ronquillo Rivera <josrr@ymail.com>
 *  This file is part of CREPL.
 *
 *  CREPL is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CREPL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CREPL.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <stdlib.h>
#include <assert.h>

#if ANDROID
#include <android/log.h>
#endif

#include <string.h>
#include <jni.h>
#include <pthread.h>
#include <stdio.h>

#include <ecl/ecl.h>
#include "ecl-boot.h"

extern cl_object standard_output;
extern cl_object error_output;

JNIEXPORT void JNICALL
Java_lisparm_clandroid_ECLTopLevelService_eclsetup(JNIEnv *env, jobject this,
						   jstring path)
{
    static int already_set = 0;
    const char *lisp_dir = (*env)->GetStringUTFChars(env, path, NULL);
    const size_t len = strlen(lisp_dir)+7;
    char *tmp; //char tmp[2048];

    if (already_set) {
	return;
    }
    already_set = 1;
    tmp = (char *)calloc(len, sizeof(char));

    LOGI("Setting the directories\n");
    snprintf(tmp, len, "%s/", lisp_dir);
    setenv("ROOT", tmp, 1);

    snprintf(tmp, len, "%s/lib/", lisp_dir);
    setenv("ECLDIR", tmp, 1);

    snprintf(tmp, len, "%s/etc/", lisp_dir);
    setenv("ETC", tmp, 1);

    snprintf(tmp, len, "%s/home/", lisp_dir);
    setenv("HOME", tmp, 1);

    free((void *)tmp);
}

JNIEXPORT void JNICALL
Java_lisparm_clandroid_ECLTopLevelService_eclstart(JNIEnv *env, jobject this,
						   jstring path)
{
    LOGI("INIT ECL");
    const char *lisp_dir = (*env)->GetStringUTFChars(env, path, NULL);
    LOGI("ECL starting: *default-pathname-defaults* to: %s\n", lisp_dir);
    ecl_boot(lisp_dir);
    LOGI("ECL started.");
}


/* This was fun to make UTF8 work across Java-C-Lisp boundaries.
   -evrim, 2014. */
cl_object java_string_to_ecl_string(JNIEnv *env, jstring str)
{
    const jchar *txt = (*env)->GetStringChars(env, str, NULL);
    jsize len = (*env)->GetStringLength(env, str);
    cl_object ecl_txt = ecl_alloc_simple_extended_string(len);
    cl_index i;

    for (i=0;i<len;i++)
	ecl_txt->string.self[i] = txt[i];

    (*env)->ReleaseStringChars(env, str, txt);

    return ecl_txt;
}

jstring ecl_object_to_java_string(JNIEnv *env, cl_object o)
{
    jstring ret;
    if (ECL_EXTENDED_STRING_P(o)) {
	LOGI("ecl->java extended string of fillp: %d, dim: %d",
	     o->string.fillp,
	     o->string.dim);
	jsize len = o->string.fillp;
	jchar *arr = malloc(sizeof(jchar)*(len+1));
	cl_index i;
	for (i=0; i<len; i++) {
	    arr[i] = o->string.self[i];
	}
	arr[len] = 0;
	ret = (*env)->NewString(env, arr, len);
	free(arr);
    } else if (ECL_STRINGP(o)) {
	LOGI("ecl->java base string of len %d: %s",
	     o->base_string.dim,
	     o->base_string.self);
	ret = (*env)->NewStringUTF(env, (const char*)o->base_string.self);
    } else {
	LOGI("ecl->java not a string, coercing");
	return ecl_object_to_java_string(env, cl_princ_to_string(o));
    }

    return ret;
}

JNIEXPORT jstring JNICALL
Java_lisparm_clandroid_ECLTopLevelService_eclexec(JNIEnv * env, jobject obj,
						  jstring str)
{
    jstring ret = NULL;
    cl_object txt = java_string_to_ecl_string(env, str);
    cl_object result = cl_safe_eval(si_string_to_object(1,txt),
				    Cnil, OBJNULL);

    LOGI("    result: %p", result);
    if ( result ) {
	ret = ecl_object_to_java_string(env, result);
	cl_force_output(1,standard_output);
	if ( get_stream_buffer(standard_output) != Cnil ) {
	    ret = print_stream_buffer(env, standard_output);
	}
    }

    cl_force_output(1,error_output);
    if ( get_stream_buffer(error_output) != Cnil ) {
	LOGI("  ERROR-OUTPUT");
	ret = print_stream_buffer(env, error_output);
    }
    LOGI("    ret: %p", ret);
    return ret;
}
