# AI-Powered-Task-Management-Portal---Backend
# 🚀 TaskFlow Backend

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-red.svg)](https://jwt.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📋 Overview

**TaskFlow Backend** is a robust REST API built with Spring Boot that powers the TaskFlow task management application. It provides secure authentication, task management, AI-powered features using Google Gemini, and an immutable blockchain audit trail for all task changes.

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based secure authentication
- User registration and login
- Role-based access control
- Password encryption with BCrypt

### 📝 Task Management
- Complete CRUD operations for tasks
- Task priority levels (LOW, MEDIUM, HIGH, CRITICAL)
- Task status tracking (TODO, IN_PROGRESS, DONE)
- Due date and estimated hours
- Search and filter capabilities
- Pagination support

### 🤖 AI Integration
- Google Gemini AI integration for task description generation
- Smart priority and time estimation
- Task completion guidance
- Productivity insights and analytics

### 🔗 Blockchain Audit Trail
- Immutable record of all task changes
- SHA-256 hashing for block integrity
- Chain verification endpoint
- Per-task independent blockchain (each task starts from block 1)
- Complete task history with timestamps

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming Language |
| Spring Boot | 3.2.5 | Framework |
| Spring Security | 3.2.5 | Authentication |
| Spring Data JPA | 3.2.5 | Database ORM |
| MySQL | 8.0 | Database |
| JWT | 0.11.5 | Token Authentication |
| Google Gemini AI | - | AI Capabilities |
| Maven | 3.9+ | Build Tool |

## 🔧 Installation

### 1. Clone the repository
```bash
git clone https://github.com/yedamaakhil/AI-Powered-Task-Management-Portal---Backend.git
cd AI-Powered-Task-Management-Portal---Backend
