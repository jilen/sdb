CREATE TABLE `order1` (
`user_id` bigint(20) NOT NULL,
`total_fee` bigint(20) NOT NULL,
`gmt_create` datetime NOT NULL,
`gmt_modified` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `name` varchar(64) NOT NULL,
  `birth` datetime NOT NULL,
  `remain` bigint(20) NOT NULL,
  `gmt_create` datetime NOT NULL,
  `gmt_modified` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
