;; -*- coding: utf-8 -*-

;;;; Copyright 2015-2016 José Ronquillo Rivera <josrr@ymail.com>
;;;; This file is part of CREPL.
;;;;
;;;; CREPL is free software: you can redistribute it and/or modify
;;;; it under the terms of the GNU General Public License as published by
;;;; the Free Software Foundation, either version 3 of the License, or
;;;; (at your option) any later version.
;;;;
;;;; CREPL is distributed in the hope that it will be useful,
;;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;; GNU General Public License for more details.
;;;;
;;;; You should have received a copy of the GNU General Public License
;;;; along with CREPL.  If not, see <http://www.gnu.org/licenses/>.

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
