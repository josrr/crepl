;; -*- coding: utf-8-unix; -*-
(defpackage #:util
  (:use #:cl)
  (:nicknames #:ut)
  (:export #:symb
	   #:defmacro/g!
	   #:defmacro!
	   #:fast-progn
	   #:safe-progn
	   #:def-respalda-func
	   #:lee-archivo
	   #:escribe-archivo
	   #:busca-archivos
	   #:fmt-fecha))

(in-package #:util)

;; Utilerías para manejar fechas

(defun fmt-fecha (&optional fecha-arg)
  "Formatea una fecha para usarla, por ejemplo, en sqlite3. Si no se pasa una
fecha, entonces usa la función get-universal-time para obtener la fecha actual."
  (let ((tiempo (if fecha-arg
		    fecha-arg
		    (get-universal-time))))
    (multiple-value-bind
	  (segundos minutos hora dia mes año dia-de-la-semana dst-p tz)
	(decode-universal-time tiempo)
      (format 'nil "~d-~2,'0d-~2,'0d ~2,'0d:~2,'0d:~2,'0d"
	      año mes dia hora minutos segundos))))

;; Utilerías para manejar archivos

(defun lee-archivo (ruta)
  "Abre un archivo, lo lee y regresa una cadena con su contenido."
  (with-open-file (archivo ruta :direction :input)
    (let ((seq (make-string (file-length archivo))))
      (read-sequence seq archivo)
      seq)) )

(defun escribe-archivo (ruta datos)
  (with-open-file (stream ruta :direction :output
			  :if-exists :supersede)
    (princ datos stream)
    (finish-output stream)))

(defun busca-archivos (ruta tipos tarea)
  (loop
     :for archivo
     :in (loop
	    :for tipo
	    :in tipos
	    :append (directory (merge-pathnames ruta tipo)))
     :do (funcall tarea archivo)))

;; La mayoría del siguiente código lo he tomado del libro Let Over Lambda, por
;; tanto el crédito es de Doug Hoyte. Para más información visita
;; http://letoverlambda.com/

;; Utilerías para influenciar al compilador

(set-dispatch-macro-character
 #\# #\F
 (lambda (stream sub-char numarg)
   (declare (ignore stream sub-char))
   (setq numarg (or numarg 3))
   (unless (<= numarg 3)
     (error "El argumento de #f debe ser un número entre 0 y 3, no ~a"
	    numarg))
   `(declare (optimize (speed ,numarg)
		       (safety ,(- 3 numarg))))))

(defmacro fast-progn (&rest body)
  `(locally #\# #\F ,@body))

(defmacro safe-progn (&rest body)
  `(locally #\# #\0 #\F ,@body))

;; Utilerías para escribir macros

(defun flatten (x)
  (labels ((rec (x acc)
             (cond ((null x) acc)
                   ((atom x) (cons x acc))
                   (t (rec
                        (car x)
                        (rec (cdr x) acc))))))
    (rec x nil)))

(defun mkstr (&rest args)
  (with-output-to-string (s)
    (dolist (a args) (princ a s))))

(defun symb (&rest args)
  (values (intern (apply #'mkstr args))))

(defun g!-symbol-p (s)
  (and (symbolp s)
       (> (length (symbol-name s)) 2)
       (string= (symbol-name s)
                "G!"
                :start1 0
                :end1 2)))

(defmacro defmacro/g! (name args &body body)
  (let ((syms (remove-duplicates
                (remove-if-not #'g!-symbol-p
                               (flatten body)))))
    `(defmacro ,name ,args
       (let ,(mapcar
               (lambda (s)
                 `(,s (gensym ,(subseq
                                 (symbol-name s)
                                 2))))
               syms)
         ,@body))))

(defun o!-symbol-p (s)
  (and (symbolp s)
       (> (length (symbol-name s)) 2)
       (string= (symbol-name s)
                "O!"
                :start1 0
                :end1 2)))

(defun o!-symbol-to-g!-symbol (s)
  (symb "G!"
        (subseq (symbol-name s) 2)))

(defmacro defmacro! (name args &rest body)
  (let* ((os (remove-if-not #'o!-symbol-p args))
         (gs (mapcar #'o!-symbol-to-g!-symbol os)))
    `(defmacro/g! ,name ,args
       `(let ,(mapcar #'list (list ,@gs) (list ,@os))
          ,(progn ,@body)))))

;; Macros

; (defmacro def-respalda-func (func-nombre func-args
; 			     &key (llave '#'car) (prueba '#'equal) (body 'nil))
;   (let ((cache-mutex-symb
; 	 (symb '* func-nombre '-ch-mutex*))
; 	(cache-symb
; 	 (symb '* func-nombre '-cache*)))
;     `(progn
;        (defvar ,cache-mutex-symb
; 	 (bordeaux-threads:make-lock
; 	  "Bloqueo automático para la función con cache"))
;        (defvar ,cache-symb ())
;        (defun ,func-nombre ,func-args
; 	 (bordeaux-threads:with-lock-held (,cache-mutex-symb)
; 	   (let ((cache-cell (assoc (funcall ,llave ,func-args)
; 				    ,cache-symb
; 				    :test ,prueba)))
; 	     (if (not cache-cell)
; 		 (let ((valor (progn (,@body))))
; 		   (setf ,cache-symb (acons ,(car func-args) (list valor) ,cache-symb))
; 		   valor )
; 		 (cadr cache-cell))))))))
; 
