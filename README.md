# AI Java Code Review & Auto-Documentation Platform

A full-stack AI-powered platform that analyzes Java source files using **AST-based parsing** and generates **structured, method-level code reviews and Javadoc** using Large Language Models (LLMs).

This project avoids unsafe string manipulation and instead relies on **JavaParser AST traversal and rewriting** for production-grade safety.

---

## 🚀 Key Features

- **AST-Based Java Parsing**
  - Safely extracts class and method structures
  - Built using JavaParser

- **AI-Powered Code Review**
  - Method-level feedback
  - Auto-generated Javadoc suggestions

- **Strict LLM JSON Contracts**
  - Machine-safe responses
  - Backend-validated AI output

- **AST-Driven Javadoc Insertion**
  - No regex or string replacement
  - Guaranteed Java syntax safety

- **Robust Error Handling**
  - Clear diagnostics for malformed Java files

- **End-to-End Workflow**
  - Upload Java source
  - Review AI feedback
  - Download updated, documented source

---

## 🧱 Architecture
React (Vite) -> Spring Boot REST API -> JavaParser AST -> LLM (Groq API) -> AST-based Source Rewriting
---

## 🛠 Tech Stack

**Backend**
- Java 17
- Spring Boot
- JavaParser

**AI / LLM**
- Groq API
- Prompt-controlled JSON output

**Frontend**
- React
- Vite

**Best Practices**
- Environment-based configuration
- Secure secret management
- Clean Git history
