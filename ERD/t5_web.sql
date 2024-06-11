SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS t5_attachment;
DROP TABLE IF EXISTS t5_user_authorities;
DROP TABLE IF EXISTS t5_authority;
DROP TABLE IF EXISTS t5_comment;
DROP TABLE IF EXISTS t5_post;
DROP TABLE IF EXISTS t5_user;




CREATE TABLE t5_attachment
(
  id         INT          NOT NULL AUTO_INCREMENT,
  post_id    INT          NOT NULL COMMENT '게시판번호',
  sourcename VARCHAR(100) NOT NULL,
  filename   VARCHAR(100) NOT NULL COMMENT '파일이름',
  PRIMARY KEY (id)
) COMMENT '파일업로드';

CREATE TABLE t5_authority
(
  id   INT         NOT NULL AUTO_INCREMENT COMMENT '권한번호',
  name VARCHAR(40) NOT NULL COMMENT '권한이름',
  PRIMARY KEY (id)
) COMMENT '권한';

ALTER TABLE t5_authority
  ADD CONSTRAINT UQ_name UNIQUE (name);

CREATE TABLE t5_comment
(
  id      INT      NOT NULL AUTO_INCREMENT COMMENT '댓글번호?',
  user_id INT      NOT NULL COMMENT '회원번호',
  post_id INT      NOT NULL COMMENT '게시판번호',
  content TEXT     NOT NULL COMMENT '댓글내용',
  regdate DATETIME NULL     DEFAULT now() COMMENT '작성일',
  PRIMARY KEY (id)
) COMMENT '댓글';

CREATE TABLE t5_post
(
  id      INT          NOT NULL AUTO_INCREMENT COMMENT '게시판번호',
  user_id INT          NOT NULL COMMENT '회원번호',
  subject VARCHAR(200) NOT NULL COMMENT '제목',
  content LONGTEXT     NULL     COMMENT '내용',
  viewcnt int          NULL     DEFAULT 0 COMMENT '조회수',
  regdate DATETIME     NULL     DEFAULT now() COMMENT '작성일',
  PRIMARY KEY (id)
) COMMENT '게시판';

CREATE TABLE t5_user
(
  id       INT          NOT NULL AUTO_INCREMENT COMMENT '회원번호',
  username VARCHAR(100) NOT NULL COMMENT '아이디',
  password VARCHAR(100) NOT NULL COMMENT '비밀번호',
  name     VARCHAR(80)  NOT NULL COMMENT '이름',
  email    VARCHAR(80)  NULL     COMMENT '이메일',
  regdate   DATETIME     NULL     DEFAULT now() COMMENT '가입일',
  PRIMARY KEY (id)
) COMMENT '회원가입';

ALTER TABLE t5_user
  ADD CONSTRAINT UQ_username UNIQUE (username);

CREATE TABLE t5_user_authorities
(
  user_id      INT NOT NULL COMMENT '회원번호',
  authority_id INT NOT NULL COMMENT '권한번호',
  PRIMARY KEY (user_id, authority_id)
);

ALTER TABLE t5_user_authorities
  ADD CONSTRAINT FK_t5_user_TO_t5_user_authorities
    FOREIGN KEY (user_id)
    REFERENCES t5_user (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE ;

ALTER TABLE t5_user_authorities
  ADD CONSTRAINT FK_t5_authority_TO_t5_user_authorities
    FOREIGN KEY (authority_id)
    REFERENCES t5_authority (id)
    ON UPDATE RESTRICT
    ON DELETE CASCADE ;

ALTER TABLE t5_post
  ADD CONSTRAINT FK_t5_user_TO_t5_post
    FOREIGN KEY (user_id)
    REFERENCES t5_user (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE ;

ALTER TABLE t5_comment
  ADD CONSTRAINT FK_t5_user_TO_t5_comment
    FOREIGN KEY (user_id)
    REFERENCES t5_user (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE ;

ALTER TABLE t5_comment
  ADD CONSTRAINT FK_t5_post_TO_t5_comment
    FOREIGN KEY (post_id)
    REFERENCES t5_post (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE ;

ALTER TABLE t5_attachment
  ADD CONSTRAINT FK_t5_post_TO_t5_attachment
    FOREIGN KEY (post_id)
    REFERENCES t5_post (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE ;
