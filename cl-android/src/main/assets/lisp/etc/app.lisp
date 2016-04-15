;; -*- coding: utf-8 -*-
(defpackage #:app
  (:export #:*dir*
	   #:logmsg
	   #:get-quicklisp))
(in-package #:app)
(defparameter *dir* *default-pathname-defaults*)

(defun logmsg (mensaje &rest args)
  (with-open-file (st (merge-pathnames #P"crepl.log" *dir*)
		      :direction :output
		      :if-does-not-exist :create
		      :if-exists :append)
    (apply #'format st (concatenate 'string "~10D: " mensaje "~%")
	   (get-universal-time) args)
    (finish-output st)))

(defun get-quicklisp ()
  (format t "Loading the quicklisp subsystem~%")
  (require '#:ecl-quicklisp)
  (require '#:deflate)
  (require '#:ql-minitar)
  (eval (read-from-string
         "(setf (symbol-function 'ql-gunzipper:gunzip) #'deflate:gunzip))")))
