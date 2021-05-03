-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 03-Maio-2021 às 23:14
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
-- Banco de dados: `g07_local`
--
CREATE DATABASE IF NOT EXISTS `g07_local` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `g07_local`;

-- --------------------------------------------------------

--
-- Estrutura da tabela `alerts`
--

DROP TABLE IF EXISTS `alerts`;
CREATE TABLE `alerts` (
  `id` int(11) NOT NULL,
  `parameter_set_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `mensagem` varchar(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Acionadores `alerts`
--
DROP TRIGGER IF EXISTS `existsPrevAlert`;
DELIMITER $$
CREATE TRIGGER `existsPrevAlert` BEFORE INSERT ON `alerts` FOR EACH ROW IF checkPrevAlert(NEW.parameter_set_id, 5) THEN
SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = 'A similar previous alert already exists in past 5 minute(s)';
END IF
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `cultures`
--

DROP TABLE IF EXISTS `cultures`;
CREATE TABLE `cultures` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `zone_id` int(11) NOT NULL,
  `manager_id` int(1) NOT NULL,
  `state` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_params`
--

DROP TABLE IF EXISTS `culture_params`;
CREATE TABLE `culture_params` (
  `id` int(11) NOT NULL,
  `sensor_type` varchar(1) NOT NULL,
  `valmax` double(5,2) NOT NULL,
  `valmin` double(5,2) NOT NULL,
  `tolerance` double(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_params_sets`
--

DROP TABLE IF EXISTS `culture_params_sets`;
CREATE TABLE `culture_params_sets` (
  `id` int(11) NOT NULL,
  `culture_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_users`
--

DROP TABLE IF EXISTS `culture_users`;
CREATE TABLE `culture_users` (
  `culture_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `measurements`
--

DROP TABLE IF EXISTS `measurements`;
CREATE TABLE `measurements` (
  `id` varchar(32) NOT NULL,
  `value` double DEFAULT NULL,
  `sensor_id` int(11) NOT NULL,
  `zone_id` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp(),
  `isCorrect` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `rel_culture_params_set`
--

DROP TABLE IF EXISTS `rel_culture_params_set`;
CREATE TABLE `rel_culture_params_set` (
  `set_id` int(11) NOT NULL,
  `culture_param_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Índices para tabelas despejadas
--

--
-- Índices para tabela `alerts`
--
ALTER TABLE `alerts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `parameter_alert` (`parameter_set_id`);

--
-- Índices para tabela `cultures`
--
ALTER TABLE `cultures`
  ADD PRIMARY KEY (`id`),
  ADD KEY `culture_zone` (`zone_id`),
  ADD KEY `culture_manager` (`manager_id`);

--
-- Índices para tabela `culture_params`
--
ALTER TABLE `culture_params`
  ADD PRIMARY KEY (`id`);

--
-- Índices para tabela `culture_params_sets`
--
ALTER TABLE `culture_params_sets`
  ADD PRIMARY KEY (`id`),
  ADD KEY `culture_id` (`culture_id`);

--
-- Índices para tabela `culture_users`
--
ALTER TABLE `culture_users`
  ADD PRIMARY KEY (`culture_id`,`user_id`),
  ADD KEY `culture` (`culture_id`),
  ADD KEY `culture_users_ibfk_1` (`user_id`);

--
-- Índices para tabela `measurements`
--
ALTER TABLE `measurements`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sensure_measure` (`sensor_id`),
  ADD KEY `sensor_zone` (`zone_id`);

--
-- Índices para tabela `rel_culture_params_set`
--
ALTER TABLE `rel_culture_params_set`
  ADD PRIMARY KEY (`set_id`,`culture_param_id`),
  ADD KEY `culture_param_id` (`culture_param_id`);

--
-- Índices para tabela `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT de tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `alerts`
--
ALTER TABLE `alerts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `cultures`
--
ALTER TABLE `cultures`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `culture_params`
--
ALTER TABLE `culture_params`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `culture_params_sets`
--
ALTER TABLE `culture_params_sets`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Restrições para despejos de tabelas
--

--
-- Limitadores para a tabela `alerts`
--
ALTER TABLE `alerts`
  ADD CONSTRAINT `parameter_alert` FOREIGN KEY (`parameter_set_id`) REFERENCES `culture_params_sets` (`id`);

--
-- Limitadores para a tabela `cultures`
--
ALTER TABLE `cultures`
  ADD CONSTRAINT `culture_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`);

--
-- Limitadores para a tabela `culture_params_sets`
--
ALTER TABLE `culture_params_sets`
  ADD CONSTRAINT `culture_params_sets_ibfk_1` FOREIGN KEY (`culture_id`) REFERENCES `cultures` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limitadores para a tabela `culture_users`
--
ALTER TABLE `culture_users`
  ADD CONSTRAINT `culture` FOREIGN KEY (`culture_id`) REFERENCES `cultures` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `culture_users_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Limitadores para a tabela `rel_culture_params_set`
--
ALTER TABLE `rel_culture_params_set`
  ADD CONSTRAINT `rel_culture_params_set_ibfk_1` FOREIGN KEY (`culture_param_id`) REFERENCES `culture_params` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `rel_culture_params_set_ibfk_2` FOREIGN KEY (`set_id`) REFERENCES `culture_params_sets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
