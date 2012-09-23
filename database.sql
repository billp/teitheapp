CREATE TABLE bus_line (
`id` int not null auto_increment,
`update_time` datetime not null,
`starting_point` int not null,
`progress` int not null,
PRIMARY KEY(`id`)
);

CREATE TABLE students_online (
`id` int not null auto_increment,
`name` varchar(30),
`surname` varchar(30),
`update_time` datetime not null,
PRIMARY KEY(`id`)
);

CREATE TABLE chat (
`id` int not null auto_increment,
`student_id` int not null,
`text` text not null,
PRIMARY KEY (`id`, `student_id`)
);
