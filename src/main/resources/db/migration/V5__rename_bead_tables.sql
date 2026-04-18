-- =====================================================
-- 拼豆V1.0 优化核心数据表名和备注
-- Date: 2026-04-18
-- 说明：让表名和备注更清晰，反映关联关系
-- =====================================================

-- 1. bead_brand -> 保持不变，备注优化
ALTER TABLE `bead_brand` COMMENT = '拼豆品牌表（如MARD、LEGO等）';

-- 2. bead_brand_kit -> 保持不变，备注优化
-- 一个品牌有多个套装，如MARD有24色、48色、64色等套装
ALTER TABLE `bead_brand_kit` COMMENT = '品牌套装表（关联bead_brand，一对多）';

-- 3. bead_palette -> 保持不变，备注优化
-- 一个色盘包含多个颜色，如A盘、B盘等
ALTER TABLE `bead_palette` COMMENT = '色盘表（一个色盘包含多个颜色）';

-- 4. bead_palette_color -> 保持不变，备注优化
-- 关联色盘和颜色，支持一个颜色属于多个色盘
ALTER TABLE `bead_palette_color` COMMENT = '色盘颜色关联表（bead_palette ↔ bead_color，多对多）';

-- 5. bead_color -> 保持不变，备注优化
-- 全局颜色定义，包含色码编号和RGB值
ALTER TABLE `bead_color` COMMENT = '拼豆瓣颜色定义表（全局颜色库，包含色码编号）';

-- 6. bead_brand_rgb_code -> 保持不变，备注优化
-- 品牌专属RGB映射，同一颜色在不同品牌可能有不同色码
-- 例如：MARD品牌的"001"和LEGO品牌的"001"可能是不同的颜色
ALTER TABLE `bead_brand_rgb_code` COMMENT = '品牌色码RGB映射表（bead_brand × bead_color，品牌专属色码）';
