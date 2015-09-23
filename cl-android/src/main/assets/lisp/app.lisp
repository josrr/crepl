;; -*- coding: utf-8 -*-
(defpackage #:app
  (:export #:*dir*
	   #:logmsg))
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
