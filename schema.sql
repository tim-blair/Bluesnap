drop table if exists players cascade;

create table players (
	id serial, --int not null auto_increment,
	name varchar(100) not null,
	primary key (id)
);

drop table if exists games cascade;

create table games (
	id serial, --int not null auto_increment,
	time timestamp not null,
	primary key (id)
);

--drop type if exists status cascade;
--create type status as enum('yes', 'no', 'maybe', 'no_response');

drop table if exists availability cascade;

create table availability (
	status varchar(20) not null default 'no_response',
	game int references games(id) not null,
	player int references players(id) not null,
	primary key (game, player)
);

