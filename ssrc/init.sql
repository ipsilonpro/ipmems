/*
 * IPMEMS, the universal cross-platform data acquisition software.
 * IPMEMS DATABASE MODEL.
 * Copyright (C) 2011, 2012 ipsilon-pro LLC.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

SET DATABASE DEFAULT TABLE TYPE CACHED;
CREATE SCHEMA IPMEMS AUTHORIZATION DBA;
SET SCHEMA IPMEMS;
SET INITIAL SCHEMA IPMEMS;
SET DATABASE DEFAULT INITIAL SCHEMA IPMEMS;
SET FILES NIO FALSE;
SET FILES WRITE DELAY TRUE;
SET FILES LOG SIZE 16;
SET FILES SCALE 64;
SET FILES DEFRAG 33;
SET DATABASE DEFAULT RESULT MEMORY ROWS 100000;
SET DATABASE TRANSACTION CONTROL LOCKS;

/* STORE TABLES */

CREATE TABLE ipTmpDblNumStore (id BIGINT, t TIMESTAMP, val DOUBLE, CONSTRAINT stmdc UNIQUE (id, t));
CREATE TABLE ipTmpStringStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR, CONSTRAINT stmtc UNIQUE (id, t));
CREATE TABLE ipTmpBoolStore (id BIGINT, t TIMESTAMP, val BOOLEAN, CONSTRAINT stmlc UNIQUE (id, t));

CREATE TABLE ipTmpDblNumArrStore (id BIGINT, t TIMESTAMP, val DOUBLE ARRAY, CONSTRAINT stmdac UNIQUE (id, t));
CREATE TABLE ipTmpStringArrStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR ARRAY, CONSTRAINT stmtac UNIQUE (id, t));
CREATE TABLE ipTmpBoolArrStore (id BIGINT, t TIMESTAMP, val BOOLEAN ARRAY, CONSTRAINT stmlac UNIQUE (id, t));

CREATE TABLE ipTmpDynDblNumStore (id BIGINT, t TIMESTAMP, val DOUBLE, CONSTRAINT stxdc UNIQUE (id));
CREATE TABLE ipTmpDynStringStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR, CONSTRAINT stxtc UNIQUE (id));
CREATE TABLE ipTmpDynBoolStore (id BIGINT, t TIMESTAMP, val BOOLEAN, CONSTRAINT stxlc UNIQUE (id));

CREATE TABLE ipTmpDynDblNumArrStore (id BIGINT, t TIMESTAMP, val DOUBLE ARRAY, CONSTRAINT stxdac UNIQUE (id));
CREATE TABLE ipTmpDynStringArrStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR ARRAY, CONSTRAINT stxtac UNIQUE (id));
CREATE TABLE ipTmpDynBoolArrStore (id BIGINT, t TIMESTAMP, val BOOLEAN ARRAY, CONSTRAINT stxlac UNIQUE (id));

CREATE TABLE ipDblNumStore (id BIGINT, t TIMESTAMP, val DOUBLE);
CREATE TABLE ipStringStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR);
CREATE TABLE ipBoolStore (id BIGINT, t TIMESTAMP, val BOOLEAN);
CREATE INDEX ipDblNumStoreIdx ON ipDblNumStore (id, t);
CREATE INDEX ipStringStoreIdx ON ipStringStore (id, t);
CREATE INDEX ipBoolStoreIdx ON ipBoolStore (id, t);

CREATE TABLE ipDblNumArrStore (id BIGINT, t TIMESTAMP, val DOUBLE ARRAY);
CREATE TABLE ipStringArrStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR ARRAY);
CREATE TABLE ipBoolArrStore (id BIGINT, t TIMESTAMP, val BOOLEAN ARRAY);
CREATE INDEX ipDblNumArrStoreIdx ON ipDblNumArrStore (id, t);
CREATE INDEX ipStringArrStoreIdx ON ipStringArrStore (id, t);
CREATE INDEX ipBoolArrStoreIdx ON ipBoolArrStore (id, t);

CREATE TABLE ipDynDblNumStore (id BIGINT, t TIMESTAMP, val DOUBLE, CONSTRAINT sxdc UNIQUE (id));
CREATE TABLE ipDynStringStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR, CONSTRAINT sxtc UNIQUE (id));
CREATE TABLE ipDynBoolStore (id BIGINT, t TIMESTAMP, val BOOLEAN, CONSTRAINT sxlc UNIQUE (id));

CREATE TABLE ipDynDblNumArrStore (id BIGINT, t TIMESTAMP, val DOUBLE ARRAY, CONSTRAINT sxdac UNIQUE (id));
CREATE TABLE ipDynStringArrStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR ARRAY, CONSTRAINT sxtac UNIQUE (id));
CREATE TABLE ipDynBoolArrStore (id BIGINT, t TIMESTAMP, val BOOLEAN ARRAY, CONSTRAINT sxlac UNIQUE (id));

CREATE TABLE ipRepDynDblNumStore (id BIGINT, t TIMESTAMP, val DOUBLE, CONSTRAINT srxdc UNIQUE (id));
CREATE TABLE ipRepDynStringStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR, CONSTRAINT srxtc UNIQUE (id));
CREATE TABLE ipRepDynBoolStore (id BIGINT, t TIMESTAMP, val BOOLEAN, CONSTRAINT srxlc UNIQUE (id));

CREATE TABLE ipRepDynDblNumArrStore (id BIGINT, t TIMESTAMP, val DOUBLE ARRAY, CONSTRAINT srxdac UNIQUE (id));
CREATE TABLE ipRepDynStringArrStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR ARRAY, CONSTRAINT srxtac UNIQUE (id));
CREATE TABLE ipRepDynBoolArrStore (id BIGINT, t TIMESTAMP, val BOOLEAN ARRAY, CONSTRAINT srxlac UNIQUE (id));

CREATE TABLE ipRepDblNumStore (id BIGINT, t TIMESTAMP, val DOUBLE);
CREATE TABLE ipRepStringStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR);
CREATE TABLE ipRepBoolStore (id BIGINT, t TIMESTAMP, val BOOLEAN);
CREATE INDEX ipRepDblNumStoreIdx ON ipRepDblNumStore (id, t);
CREATE INDEX ipRepStringStoreIdx ON ipRepStringStore (id, t);
CREATE INDEX ipRepBoolStoreIdx ON ipRepBoolStore (id, t);

CREATE TABLE ipRepDblNumArrStore (id BIGINT, t TIMESTAMP, val DOUBLE ARRAY);
CREATE TABLE ipRepStringArrStore (id BIGINT, t TIMESTAMP, val LONGVARCHAR ARRAY);
CREATE TABLE ipRepBoolArrStore (id BIGINT, t TIMESTAMP, val BOOLEAN ARRAY);
CREATE INDEX ipRepDblNumArrStoreIdx ON ipRepDblNumArrStore (id, t);
CREATE INDEX ipRepStringArrStoreIdx ON ipRepStringArrStore (id, t);
CREATE INDEX ipRepBoolArrStoreIdx ON ipRepBoolArrStore (id, t);

CREATE TABLE ipLog (aid BIGINT, t TIMESTAMP, msg VARCHAR(512), args LONGVARCHAR ARRAY);
CREATE INDEX ipLogIdx ON ipLog (aid, t);
CREATE INDEX ipLogTempIdx ON ipLog (t);
CREATE INDEX ipLogMsgIdx ON ipLog (msg, t);

/* OBJECT TABLES */

CREATE TABLE ipParents (pid BIGINT, cid BIGINT, CONSTRAINT parc UNIQUE (pid, cid));
CREATE TABLE ipObjects (id BIGINT, n VARCHAR(1024), t VARCHAR(128), CONSTRAINT oc UNIQUE (id));
CREATE TABLE ipValues (id BIGINT, k VARCHAR(32), val LONGVARCHAR, CONSTRAINT vc UNIQUE (id, k));
CREATE INDEX ipParentIdx ON ipParents (cid, pid);
CREATE INDEX ipValueIdx ON ipValues (k, val);