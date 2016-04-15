;; -*- coding: utf-8-unix; -*-

;;;; Copyright 2015 José Ronquillo Rivera <josrr@ymail.com>
;;;; This file is part of cl-util.
;;;;
;;;; cl-util is free software: you can redistribute it and/or modify
;;;; it under the terms of the GNU General Public License as published by
;;;; the Free Software Foundation, either version 3 of the License, or
;;;; (at your option) any later version.
;;;;
;;;; cl-util is distributed in the hope that it will be useful,
;;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;; GNU General Public License for more details.
;;;;
;;;; You should have received a copy of the GNU General Public License
;;;; along with cl-util.  If not, see <http://www.gnu.org/licenses/>.
(in-package #:common-lisp-user)
(asdf:defsystem #:util
  :serial t
  :description "Algunas utilerías generales y básicas."
  :author "José Miguel Ronquillo Rivera"
  :license "GPLv3"
  :components ((:file "util")))
