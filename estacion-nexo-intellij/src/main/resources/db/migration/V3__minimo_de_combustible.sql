ALTER TABLE producto ADD stock_minimo DECIMAL(12,3) NULL CHECK (stock_minimo >= 0);
