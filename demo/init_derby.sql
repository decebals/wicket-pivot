DROP TABLE STATISTIC;

CREATE TABLE STATISTIC (
  REGION varchar(20),
  SALESMAN varchar(20),
  "YEAR" int,
  MONTH int,
  SALES int,
  COST int
);

INSERT INTO STATISTIC (REGION, SALESMAN, "YEAR", MONTH, SALES, COST) VALUES 
('1', 'John', 2005, 3, 1000, 100),
('1', 'David', 2005, 3, 1300, 200),
('1', 'Sophia', 2005, 1, 312, 400),
('2', 'David', 2005, 3, 1234, 0),
('2', 'Sophia', 2006, 3, 543, 0),
('3', 'John', 2006, 4, 534, 0),
('3', 'David', 2006, 6, 423, 0),
('3', 'John', 2007, 8, 134, 0),
('4', 'Sophia', 2007, 4, 423, 0),
('4', 'David', 2007, 2, 466, 0),
('4', 'David', 2008, 4, 563, 0),
('4', 'John', 2008, 5, 222, 0),
('1', 'Oliver', 2005, 2, 0, 0)
