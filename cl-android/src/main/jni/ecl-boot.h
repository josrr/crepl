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
