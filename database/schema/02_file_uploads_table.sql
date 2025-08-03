-- File uploads table creation
-- Purpose: Track CSV file upload metadata
-- Dependencies: users table must exist

USE salesdata;

CREATE TABLE file_uploads (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              user_id BIGINT NOT NULL,
                              original_filename VARCHAR(255) NOT NULL,
                              upload_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              total_rows INT NOT NULL DEFAULT 0,
                              records_processed INT NOT NULL DEFAULT 0,
                              records_failed INT NOT NULL DEFAULT 0,
                              error_message TEXT,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              processed_at TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add indexes
CREATE INDEX idx_file_uploads_user_id ON file_uploads(user_id);
CREATE INDEX idx_file_uploads_upload_status ON file_uploads(upload_status);