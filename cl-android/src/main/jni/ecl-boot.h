#ifndef _ECL_BOOT_H_
#define _ECL_BOOT_H_

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

int ecl_boot(const char *root_dir);
void ecl_toplevel(const char *home);
void eclshell_show(char *message);
cl_object get_stream_buffer(cl_object stream);
jstring print_stream_buffer(JNIEnv *env, cl_object stream);


#endif
