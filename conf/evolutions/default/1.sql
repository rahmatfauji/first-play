# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

-- init script create procs
-- Inital script to create stored procedures etc for mysql platform
DROP PROCEDURE IF EXISTS usp_ebean_drop_foreign_keys;

delimiter $$
--
-- PROCEDURE: usp_ebean_drop_foreign_keys TABLE, COLUMN
-- deletes all constraints and foreign keys referring to TABLE.COLUMN
--
CREATE PROCEDURE usp_ebean_drop_foreign_keys(IN p_table_name VARCHAR(255), IN p_column_name VARCHAR(255))
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE c_fk_name CHAR(255);
  DECLARE curs CURSOR FOR SELECT CONSTRAINT_NAME from information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE() and TABLE_NAME = p_table_name and COLUMN_NAME = p_column_name
      AND REFERENCED_TABLE_NAME IS NOT NULL;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN curs;

  read_loop: LOOP
    FETCH curs INTO c_fk_name;
    IF done THEN
      LEAVE read_loop;
    END IF;
    SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP FOREIGN KEY ', c_fk_name);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
  END LOOP;

  CLOSE curs;
END
$$

DROP PROCEDURE IF EXISTS usp_ebean_drop_column;

delimiter $$
--
-- PROCEDURE: usp_ebean_drop_column TABLE, COLUMN
-- deletes the column and ensures that all indices and constraints are dropped first
--
CREATE PROCEDURE usp_ebean_drop_column(IN p_table_name VARCHAR(255), IN p_column_name VARCHAR(255))
BEGIN
  CALL usp_ebean_drop_foreign_keys(p_table_name, p_column_name);
  SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP COLUMN ', p_column_name);
  PREPARE stmt FROM @sql;
  EXECUTE stmt;
END
$$
create table general_information (
  id                            bigint(20) UNSIGNED auto_increment not null,
  user_id                       varchar(100),
  fullname                      varchar(100),
  gender                        varchar(100),
  dob                           varchar(100),
  phone                         varchar(100),
  address                       varchar(100),
  city                          varchar(100),
  province                      varchar(100),
  country                       varchar(100),
  constraint pk_general_information primary key (id)
);

create table roles (
  id                            bigint(20) UNSIGNED auto_increment not null,
  name                          varchar(255),
  description                   varchar(255),
  created_at                    varchar(100),
  updated_at                    varchar(100),
  constraint pk_roles primary key (id)
);

create table roles_users (
  id                            bigint(20) UNSIGNED auto_increment not null,
  roles_id                      integer not null,
  users_id                      varchar(255),
  constraint pk_roles_users primary key (id)
);

create table users (
  id                            varchar(255) not null,
  username                      varchar(100),
  email                         varchar(100),
  password                      varchar(100),
  created_at                    varchar(100),
  updated_at                    varchar(100),
  constraint pk_users primary key (id)
);

create table user_tokens (
  id                            bigint(20) auto_increment not null,
  token                         varchar(255) NOT NULL,
  issued_at                     DATETIME,
  expired_at                    DATETIME,
  user_id                       varchar(255) UNSIGNED,
  logout                        tinyint(1) not null,
  constraint uq_user_tokens_token unique (token),
  constraint pk_user_tokens primary key (id)
);


# --- !Downs

drop table if exists general_information;

drop table if exists roles;

drop table if exists roles_users;

drop table if exists users;

drop table if exists user_tokens;

