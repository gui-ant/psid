-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 21-Maio-2021 às 02:43
-- Versão do servidor: 10.4.18-MariaDB
-- versão do PHP: 7.4.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `g07_cloud`
--
CREATE DATABASE IF NOT EXISTS `g07_cloud` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `g07_cloud`;

-- --------------------------------------------------------

--
-- Estrutura da tabela `sensors`
--

DROP TABLE IF EXISTS `sensors`;
CREATE TABLE IF NOT EXISTS `sensors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(11) NOT NULL,
  `minlim` double(5,2) NOT NULL,
  `maxlim` double(5,2) NOT NULL,
  `zone_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `zone_id` (`zone_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;

--
-- Extraindo dados da tabela `sensors`
--

INSERT INTO `sensors` (`id`, `name`, `minlim`, `maxlim`, `zone_id`) VALUES
(1, 'T1', 0.00, 20.00, 1),
(2, 'T2', 5.00, 25.00, 2),
(3, 'H1', 0.00, 40.00, 1),
(4, 'H2', 10.00, 30.00, 2),
(5, 'L1', 10.00, 20.00, 1),
(6, 'L2', 20.00, 30.00, 2);

-- --------------------------------------------------------

--
-- Estrutura da tabela `zones`
--

DROP TABLE IF EXISTS `zones`;
CREATE TABLE IF NOT EXISTS `zones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) NOT NULL,
  `temperature` double(5,2) NOT NULL,
  `humidity` double(5,2) NOT NULL,
  `light` double(5,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

--
-- Extraindo dados da tabela `zones`
--

INSERT INTO `zones` (`id`, `name`, `temperature`, `humidity`, `light`) VALUES
(1, 'Z1', 10.00, 15.00, 20.00),
(2, 'Z2', 0.00, 20.00, 10.00);

--
-- Restrições para despejos de tabelas
--

--
-- Limitadores para a tabela `sensors`
--
ALTER TABLE `sensors`
  ADD CONSTRAINT `sensors_ibfk_1` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
