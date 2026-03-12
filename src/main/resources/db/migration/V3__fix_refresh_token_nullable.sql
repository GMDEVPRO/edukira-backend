-- V3: torna user_id nullable em refresh_tokens para suportar contas de alunos
ALTER TABLE refresh_tokens ALTER COLUMN user_id DROP NOT NULL;

-- Torna sender_id nullable para suportar mensagens automáticas do sistema
ALTER TABLE messages ALTER COLUMN sender_id DROP NOT NULL;