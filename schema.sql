-- ============================================================
-- QUIZMASTER UNIVERSITY
-- FINAL DATABASE SCHEMA
-- PostgreSQL 15+
-- ============================================================

-- =====================
-- DROP OLD OBJECTS
-- =====================
DROP TABLE IF EXISTS answer_selected_options CASCADE;
DROP TABLE IF EXISTS attempt_answers CASCADE;
DROP TABLE IF EXISTS quiz_attempts CASCADE;
DROP TABLE IF EXISTS question_options CASCADE;
DROP TABLE IF EXISTS questions CASCADE;
DROP TABLE IF EXISTS quiz_assignments CASCADE;
DROP TABLE IF EXISTS quizzes CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS study_groups CASCADE;

DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS question_type CASCADE;
DROP TYPE IF EXISTS attempt_status CASCADE;

-- =====================
-- ENUM TYPES
-- =====================
CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'TEACHER',
    'STUDENT'
);

CREATE TYPE question_type AS ENUM (
    'SINGLE_CHOICE',
    'MULTIPLE_CHOICE',
    'TRUE_FALSE'
);

CREATE TYPE attempt_status AS ENUM (
    'IN_PROGRESS',
    'COMPLETED',
    'TIMEOUT'
);

-- =====================
-- STUDY GROUPS
-- =====================
CREATE TABLE study_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    course_number INTEGER NOT NULL CHECK (course_number BETWEEN 1 AND 6),
    speciality VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- USERS
-- =====================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    group_id BIGINT REFERENCES study_groups(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_student_group
    CHECK (
        (role = 'STUDENT' AND group_id IS NOT NULL)
        OR
        (role IN ('ADMIN', 'TEACHER'))
    )
);

-- =====================
-- COURSES
-- =====================
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- QUIZZES
-- =====================
CREATE TABLE quizzes (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    time_limit_minutes INTEGER NOT NULL DEFAULT 30 CHECK (time_limit_minutes > 0),
    max_attempts INTEGER NOT NULL DEFAULT 1 CHECK (max_attempts > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- QUIZ ASSIGNMENTS
-- =====================
CREATE TABLE quiz_assignments (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES study_groups(id) ON DELETE CASCADE,

    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    available_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,

    UNIQUE (quiz_id, group_id),

    CONSTRAINT chk_assignment_dates
    CHECK (
        available_from <= due_date
        AND assigned_at <= due_date
    )
);

-- =====================
-- QUESTIONS
-- =====================
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    type question_type NOT NULL,
    points NUMERIC(5,2) NOT NULL DEFAULT 1 CHECK (points > 0),
    order_num INTEGER NOT NULL DEFAULT 0
);

-- =====================
-- QUESTION OPTIONS
-- =====================
CREATE TABLE question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE
);

-- =====================
-- QUIZ ATTEMPTS
-- =====================
CREATE TABLE quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,

    score NUMERIC(6,2) NOT NULL DEFAULT 0,
    max_score NUMERIC(6,2) NOT NULL DEFAULT 0,

    status attempt_status NOT NULL DEFAULT 'IN_PROGRESS',

    CONSTRAINT chk_attempt_score
    CHECK (
        score >= 0
        AND max_score >= 0
        AND score <= max_score
    ),

    CONSTRAINT chk_finish_after_start
    CHECK (
        finished_at IS NULL
        OR finished_at >= started_at
    )
);

-- =====================
-- ATTEMPT ANSWERS
-- =====================
CREATE TABLE attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    score NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (score >= 0),

    UNIQUE (attempt_id, question_id)
);

-- =====================
-- ANSWER SELECTED OPTIONS
-- =====================
CREATE TABLE answer_selected_options (
    answer_id BIGINT NOT NULL REFERENCES attempt_answers(id) ON DELETE CASCADE,
    option_id BIGINT NOT NULL REFERENCES question_options(id) ON DELETE CASCADE,

    PRIMARY KEY (answer_id, option_id)
);

-- =====================
-- INDEXES
-- =====================
CREATE INDEX idx_users_group_id
ON users(group_id);

CREATE INDEX idx_courses_teacher_id
ON courses(teacher_id);

CREATE INDEX idx_quizzes_course_id
ON quizzes(course_id);

CREATE INDEX idx_quiz_assignments_quiz_id
ON quiz_assignments(quiz_id);

CREATE INDEX idx_quiz_assignments_group_id
ON quiz_assignments(group_id);

CREATE INDEX idx_quiz_assignments_due_date
ON quiz_assignments(due_date);

CREATE INDEX idx_questions_quiz_id
ON questions(quiz_id);

CREATE INDEX idx_question_options_question_id
ON question_options(question_id);

CREATE INDEX idx_quiz_attempts_quiz_id
ON quiz_attempts(quiz_id);

CREATE INDEX idx_quiz_attempts_student_id
ON quiz_attempts(student_id);

CREATE INDEX idx_quiz_attempts_status
ON quiz_attempts(status);

CREATE INDEX idx_attempt_answers_attempt_id
ON attempt_answers(attempt_id);

CREATE INDEX idx_attempt_answers_question_id
ON attempt_answers(question_id);

CREATE INDEX idx_answer_selected_options_option_id
ON answer_selected_options(option_id);