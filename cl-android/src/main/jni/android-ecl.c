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

#if ANDROID
#define ECL_TAG "ecl-native"
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, ECL_TAG, __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, ECL_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, ECL_TAG, __VA_ARGS__))
#define LOGV(...) ((void)__android_log_print(ANDROID_LOG_VERBOSE, ECL_TAG, __VA_ARGS__))
#else
#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#endif

extern cl_object standard_output;
extern cl_object error_output;

JNIEXPORT void JNICALL
Java_lisparm_clandroid_ECLTopLevelService_startECL(JNIEnv *env, jobject this, jstring path)
{
    LOGI("INIT ECL");
    const char *lisp_dir = (*env)->GetStringUTFChars(env, path, NULL);
    LOGI("ECL starting: *default-pathname-defaults* to: %s\n", lisp_dir);
    ecl_boot(lisp_dir);
    LOGI("ECL started.");
}


/* This was fun to make UTF8 work across Java-C-Lisp boundaries.
   -evrim, 2014. */
cl_object java_string_to_ecl_string(JNIEnv *env, jstring str) {
  const jchar *txt = (*env)->GetStringChars(env, str, NULL);
  jsize len = (*env)->GetStringLength(env, str);
  cl_object ecl_txt = ecl_alloc_simple_extended_string(len);
  cl_index i;

  for (i=0;i<len;i++) {
    ecl_txt->string.self[i] = txt[i];
  };

  (*env)->ReleaseStringChars(env, str, txt);

  return ecl_txt;
}


jstring ecl_object_to_java_string(JNIEnv *env, cl_object o) {
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

    ret = (*env)->NewStringUTF(env,
			       (const char*)o->base_string.self);
  } else {
    LOGI("ecl->java not a string, coercing");
    return ecl_object_to_java_string(env, cl_princ_to_string(o));
  }

  return ret;
}


JNIEXPORT jstring JNICALL
Java_lisparm_clandroid_ECLTopLevelService_eclExec (JNIEnv * env, jobject obj, jstring str)
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

    return ret;
}
