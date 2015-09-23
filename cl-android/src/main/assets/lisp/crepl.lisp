;; -*- coding: utf-8-unix; -*-

;;;; Copyright (C) 2015 José Ronquillo Rivera <josrr@ymail.com>
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

;;; (ext:set-limit 'ext:binding-stack 319200)

(defpackage #:crepl
  (:use #:common-lisp)
  (:export #:*lines*
	   #:execute-sexp
	   #:set-data
	   #:get-lines
	   #:delete-lines
	   #:crepl-output-stream
	   #:crepl-input-stream))

(in-package #:crepl)

(defparameter *lines* (make-hash-table))

;;;;;;;;;;;;;;;;

(defclass crepl-stream ()
  ((buffer :accessor crepl-stream-buffer
	   :initform (make-array 0 :adjustable t
				 :fill-pointer t
				 :element-type 'character))
   (dirty   :accessor dirty-p
	    :initform nil)
   (name :accessor crepl-stream-name
	 :initarg :name
	 :initform ""))
  (:documentation "A stream conected to CREPL"))

;;;;;;;;;;;;;;;;

(defclass crepl-output-stream (gray:fundamental-character-output-stream
			       crepl-stream)
  ()
  #|((lines :accessor lines
	  :initform (make-hash-table)))|#
  (:documentation "An output-stream conected to CREPL"))

(defun get-lines (stream)
  (let ((lines (gethash stream *lines*)))
    ;;(app:logmsg "get-lines ~S:~S" (crepl-stream-name stream) lines)
    (if lines
	(apply #'concatenate 'string
	       (reverse lines)))))

(defun delete-lines (stream)
  (setf (gethash stream *lines*) nil))

(defmethod ext::stream-close ((stream crepl-output-stream))
  (error "Cannot close the stream ~S." (crepl-stream-buffer stream)))

(defmethod gray:stream-force-output ((stream crepl-output-stream))
  ;;(app:logmsg "gray:stream-force-output: ~S   dirty: ~A" (crepl-stream-name stream) (dirty-p stream))
  (when (dirty-p stream)
    ;;(app:logmsg " dirty ~s" (crepl-stream-buffer stream))
    (push (crepl-stream-buffer stream) (gethash stream *lines*))
    ;;(app:logmsg " *lines*: ~S" (gethash stream *lines*))
    )
  (gray:stream-clear-output stream))

(defmethod gray:stream-write-char ((stream crepl-output-stream) char)
  ;;(app:logmsg "gray:stream-write-char: ~S ~S" (crepl-stream-name stream) char)
  (setf (dirty-p stream) t)
  (vector-push-extend char (crepl-stream-buffer stream))
  (when (char= char #\Newline)
    (gray:stream-force-output stream)))

(defmethod gray:stream-clear-output ((stream crepl-output-stream))
  ;;(app:logmsg "gray:stream-clear-output: ~S   dirty: ~A" (crepl-stream-name stream) (dirty-p stream))
  (setf (crepl-stream-buffer stream) (make-array  0 :adjustable t
						  :fill-pointer t
   						  :element-type 'character)
	(dirty-p stream) nil)
  ;;(app:logmsg "   *lines*: ~S" (gethash stream *lines*))
  nil)

;;;;;;;;;;;;;;;;
(defclass crepl-input-stream (gray:fundamental-character-input-stream
			      crepl-stream)
  ()
  (:documentation "An input-stream conected to CREPL"))

(defun set-data (input-stream data)
  (loop
     for c across data
     do (vector-push-extend c (crepl-stream-buffer input-stream)))
  (setf (dirty-p input-stream) t)
  (crepl-stream-buffer input-stream))

(defmethod gray:stream-read-char ((stream crepl-input-stream))
  ;(app:logmsg "gray:stream-read-char: ~S" (crepl-stream-name stream))
  (loop while (not (dirty-p stream)))
  (vector-pop (crepl-stream-buffer stream)))

(defmethod gray:stream-unread-char ((stream crepl-input-stream) char)
  ;;(app:logmsg "gray:stream-unread-char: ~S ~S" (crepl-stream-name stream) char)
  (vector-push-extend char (crepl-stream-buffer stream))
  (setf (dirty-p stream) t)
  nil)

(defmethod gray:stream-clear-input ((stream crepl-input-stream))
  #|(app:logmsg "gray:stream-clear-input: ~S   dirty: ~A"
	      (crepl-stream-name stream)
	      (dirty-p stream))|#
  (setf (crepl-stream-buffer stream) (make-array  0 :adjustable t
						  :fill-pointer t
   						  :element-type 'character)
	(dirty-p stream) nil)
  nil)
;;;;;;;;;;;;;;;;

(defvar + nil)
(defvar ++ nil)
(defvar +++ nil)

(setf common-lisp:*standard-output* (make-instance 'crepl-output-stream :name "standard-output")
      common-lisp:*error-output* (make-instance 'crepl-output-stream :name "error-output"))

;;;common-lisp:*standard-input* (make-instance 'crepl-input-stream :name "standard-input")

(defmacro execute-sexp (sexp)
  (let ((sexp-val (gensym)))
    `(handler-case
	 (let* ((* +)
		(** ++)
		(*** +++)
		(,sexp-val ,sexp))
	   (app:logmsg "execute-sexp sexp-val: ~A~%" ,sexp-val)
	   (setf +++ ++
		 ++ +
		 + ,sexp-val)
	   ,sexp-val)
       (error (condition)
	 (app:logmsg "ERROR: ~A~%" condition)
	 (format *error-output*
		 "~&Condition of type: ~A~%~A~%" (type-of condition) condition)
	 nil)
       (serious-condition (condition)
	 (app:logmsg "SERIOUS-ERROR: ~A~%" condition)
	 (format *error-output*
		 "~&Condition of type: ~A~%~A~%" (type-of condition) condition)
	 nil))))

;;;;;

;;;;;
