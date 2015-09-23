#ifndef _ECL_BOOT_H_
#define _ECL_BOOT_H_

int ecl_boot(const char *root_dir);
void ecl_toplevel(const char *home);
void eclshell_show(char *message);
cl_object get_stream_buffer(cl_object stream);
jstring print_stream_buffer(JNIEnv *env, cl_object stream);

#endif
