;; -*- coding: utf-8 -*-
(in-package :cl-user)
(format t "ECL (Embeddable Common-Lisp) ~A (git:~D)~%"
        (lisp-implementation-version)
        (ext:lisp-implementation-vcs-id))

(require '#:asdf)
(require '#:sockets)
(require '#:serve-event)

(setf asdf:*user-cache* (merge-pathnames #P"home/cache/"
					 *default-pathname-defaults*)
      asdf:*compile-file-failure-behaviour* :error
      *default-directory* *default-pathname-defaults*)
;; (defvar *ecl-home* *default-directory*)
;; (ext:setenv "HOME" (namestring *ecl-home*))

(dolist (dir (directory (merge-pathnames "paquetes/*/"
					 *default-pathname-defaults*)))
  (pushnew (namestring dir) asdf:*central-registry*))
(pushnew (namestring (merge-pathnames "etc/"
				      *default-pathname-defaults*))
	 asdf:*central-registry*)
(pushnew (namestring (merge-pathnames "lib/"
				      *default-pathname-defaults*))
	 asdf:*central-registry*)

(load "etc/app")
(require '#:util)
(require '#:crepl)

(util:escribe-archivo #P"central-registry.txt"
		      (format nil "universal-time=~D~%~S~%"
			      (get-universal-time)
			      asdf:*central-registry*))

;; (crepl:_print "Error in eval")

;;(in-package #:crepl)
;;(require :cl-base64)
;;(require :ironclad)
#|(with-open-file (s (merge-pathnames "cl-base64.txt" app:*dir*)
:direction :output :if-exists :supersede)
(format s "universal-time=~D~%~S~%" (get-universal-time)
	cl-base64:*uri-encode-table*)
(finish-output s))|#
