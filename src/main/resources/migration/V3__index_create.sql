CREATE INDEX CONCURRENTLY idx_users_telegram_id ON users(telegram_id);
CREATE INDEX CONCURRENTLY idx_users_referral_code ON users(referral_code);
CREATE INDEX CONCURRENTLY idx_vacancies_active ON vacancies(is_active, created_at DESC);
CREATE INDEX CONCURRENTLY idx_resumes_active ON resumes(is_active, created_at DESC);
CREATE INDEX CONCURRENTLY idx_withdrawals_status ON withdrawals(status, created_at);