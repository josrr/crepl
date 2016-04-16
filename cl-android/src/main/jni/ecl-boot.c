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
#include <string.h>
#include <ecl/ecl.h>
#include <jni.h>
#if ANDROID
#include <android/log.h>
#endif

#include "ecl-boot.h"
#include "android-ecl.h"

#ifdef __cplusplus
#define ECL_CPP_TAG "C"
#else
#define ECL_CPP_TAG
#endif

/*extern ECL_CPP_TAG void init_lib_ASDF();
extern ECL_CPP_TAG void init_lib_SOCKETS();
extern ECL_CPP_TAG void init_lib_SB_BSD_SOCKETS();
extern ECL_CPP_TAG void init_lib_SERVE_EVENT();
extern ECL_CPP_TAG void init_lib_ECL_CDB();
extern ECL_CPP_TAG void init_lib_ECL_HELP();*/

cl_object standard_output;
cl_object error_output;

cl_object get_stream_buffer(cl_object stream)
{
    LOGI("get_stream_buffer %p", stream);
    cl_object name = ecl_make_symbol("GET-LINES","CREPL");
    cl_object lines = cl_funcall(2, name, stream);
    if (lines != Cnil)
	LOGI("get_stream_buffer %p %p", stream, lines);
    return lines;
}

jstring print_stream_buffer(JNIEnv *env, cl_object stream)
{
  /*cl_env_ptr eclenv = ecl_process_env();*/
  cl_object lines = get_stream_buffer(stream);
  jstring print_buffer = ecl_object_to_java_string(env, lines);
  cl_object name = ecl_make_symbol("DELETE-LINES","CREPL");
  LOGI("print_stream_buffer %p", lines);
  cl_funcall(2, name, stream);
  return print_buffer;
}

int ecl_boot(const char *root_dir)
{
    char *ecl = "ecl";

    LOGI("ROOT='%s'\n", getenv("ROOT"));
    LOGI("ECLDIR='%s'\n", getenv("ECLDIR"));

    cl_boot(1, &ecl);
    LOGI("installing bytecodes compiler\n");
    si_safe_eval(3, c_string_to_object("(si:install-bytecodes-compiler)"), ECL_NIL, OBJNULL);
    atexit(cl_shutdown);
    /* init_lib_ECL_HELP();
       init_lib_ASDF();
       init_lib_SOCKETS();
       init_lib_SB_BSD_SOCKETS(); */
    ecl_toplevel(root_dir);
    return 0;
}

void ecl_toplevel(const char *home)
{
    size_t len = strlen(home)+45;
    char *str_lisp = (char *)calloc(len, sizeof(char));
    cl_object crepl_pkg_str = ecl_make_simple_base_string("CREPL", 5);
    cl_object name_stream = ecl_make_symbol("*STANDARD-OUTPUT*","COMMON-LISP");

    LOGI("START TOP LEVEL\n");

    CL_CATCH_ALL_BEGIN(ecl_process_env()) {
	snprintf(str_lisp, len,"(setq *default-pathname-defaults* #p\"%s/\")"
		 /*"(ext:chdir #P\"%s/\")"*/, home);
	si_safe_eval(3, c_string_to_object(str_lisp), Cnil, OBJNULL);
	ecl_make_package(crepl_pkg_str, Cnil, Cnil);
	si_select_package(ecl_make_simple_base_string("CL-USER", 7));
	si_safe_eval(3, c_string_to_object("(load \"etc/inicia\")"), Cnil, OBJNULL);
	/*si_select_package(crepl_pkg_str);*/
    } CL_CATCH_ALL_END;

    LOGI("ALL LOADED\n");
    free((void *)str_lisp);
    standard_output = ecl_symbol_value(name_stream);
    name_stream = ecl_make_symbol("*ERROR-OUTPUT*","COMMON-LISP");
    error_output = ecl_symbol_value(name_stream);

    /*
    si_safe_eval(3, c_string_to_object
		 ("(format t \"features: ~A ~%\" *features*)"),
		 Cnil, OBJNULL);
    si_safe_eval(3, c_string_to_object
		 ("(format t \"(truename SYS:): ~A)\" (truename \"SYS:\"))"),
		 Cnil, OBJNULL);

    cl_force_output(1,standard_output);
    */

    LOGI("EXIT TOP LEVEL\n");
}

/* cl_object cl_user_pkg_str = ecl_make_simple_base_string("CL-USER", 7); */

/* extern void init_lib_CREPL_LISP(cl_object);
   ecl_init_module(NULL, init_lib_CREPL_LISP); */

/*si_select_package(crepl_pkg_str);
  cl_def_c_function(c_string_to_object("_print"),
  (cl_objectfn_fixed) cl_print_in_crepl, 1);*/
