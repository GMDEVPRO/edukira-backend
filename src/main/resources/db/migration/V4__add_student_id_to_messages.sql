ALTER TABLE messages ADD COLUMN IF NOT EXISTS student_id UUID;
ALTER TABLE messages ADD CONSTRAINT fk_messages_student
    FOREIGN KEY (student_id) REFERENCES students(id);