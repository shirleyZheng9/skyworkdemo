UPDATE bt_app_publish
SET url = REPLACE(
  CONCAT(
    'https://agent.artifex-cmcc.com.cn/aiengine',
    SUBSTRING(url, LOCATE('/bote/', url) + 5)
  ),
  'redirect=%2Fbote%2F',
  'redirect=%2Faiengine%2F'
)
WHERE url IS NOT NULL
  AND url LIKE '%/bote/%'
  AND url NOT LIKE 'https://agent.artifex-cmcc.com.cn/aiengine/%';
