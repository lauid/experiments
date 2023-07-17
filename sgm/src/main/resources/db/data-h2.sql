DELETE
FROM sys_user;

DELETE
FROM role;

INSERT INTO role (id, role_name, role_describe)
VALUES (1, '管理员', 'boos 级别'),
       (2, '用户', '就是个普通人'),
       (3, '程序猿', '偶尔需要用来祭天');

INSERT INTO sys_user (id, name, age, email)
VALUES (1, 'Jone', 18, 'test1@baomidou.com'),
       (2, 'Jack', 20, 'test2@baomidou.com'),
       (3, 'Tom', 28, 'test3@baomidou.com'),
       (4, 'Sandy', 21, 'test4@baomidou.com'),
       (5, 'Billie', 24, 'test5@baomidou.com');

INSERT INTO user2 (id, name, age)
VALUES (1, 'Jone', 18),
       (2, 'Jack', 20),
       (3, 'Tom', 28),
       (4, 'Sandy', 21),
       (5, 'Billie', 24);