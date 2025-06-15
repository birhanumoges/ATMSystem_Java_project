# 🏧 Java ATM System (Swing GUI)

A simple Java-based ATM system with a graphical user interface (GUI) built using **Java Swing**. It allows users to create accounts, log in using a 4-digit PIN, and perform basic banking operations like deposit, withdrawal, and balance check.

---

## 📚 Table of Contents

- [📚 Description](#-description)
- [💻 Features](#-features)
- [🚀 How to Use](#-how-to-use)
  - [🔐 Login](#-login)
  - [🧾 Create Account](#-create-account)
  - [💰 ATM Operations](#-atm-operations)
- [🛠️ Requirements](#️-requirements)
- [🏗️ Build & Run Instructions](#-build--run-instructions)
- [📁 File Structure](#-file-structure)
- [🧩 Technologies Used](#-technologies-used)
- [📌 Conclusion](#-conclusion)

---

## Description

📚This project simulates an ATM system with functionalities similar to real-world ATMs. The system uses a file-based storage mechanism (`accounts.dat`) to persist user accounts and balances. It ensures user-friendly interactivity, validation checks, and secure access via PIN.

---

## Features

- Create a new account with automatic Account Number and PIN generation.
- Login using only a **4-digit PIN**.
- Check account balance.
- Deposit money.
- Withdraw money (with minimum balance check).
- Clear input fields and Exit buttons.
- All data is saved to a file to preserve user accounts.

---

## How to Use

### 🔐 Login

- Enter your 4-digit PIN to access your account.
- Click **Login**.

### 🧾 Create Account

- Click **Create Account** on the login screen.
- Enter:
  - Your **Name**
  - **Initial Deposit** (must be ≥ 100)
- The system generates:
  - A random **Account Number**
  - A secure **4-digit PIN**
- Save your PIN for future logins.

### 💰 ATM Operations

- **View Balance**: Check your current balance.
- **Deposit**: Add funds (amount must be positive).
- **Withdraw**: Withdraw funds (minimum balance of 100 must remain).
- **Logout**: Return to the login screen.
- 💾 **Data Persistence:** Account details saved locally in `accounts.dat`.
- 🧼 **Interactive GUI:** Built using Java Swing with clear, exit, and error handling.

---

### 💻 Technologies Used

| Tech        | Description                |
|-------------|----------------------------|
| Java        | Programming Language       |
| Java Swing  | GUI framework              |
| File I/O    | Object Serialization       |

---

## Requirements

- Java Development Kit (JDK) 17 or higher (recommended)
- A Java IDE (like IntelliJ, Eclipse, NetBeans) or terminal access
- Operating System: Windows, Linux, or macOS

---

##Build & Run Instructions

### ✅ Compile

```bash
javac ATMSystem.java
