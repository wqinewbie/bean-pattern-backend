-- Remove the legacy gift table. All gift ownership and redemption state now lives in bp_user_gift.
DROP TABLE IF EXISTS `user_gift`;
