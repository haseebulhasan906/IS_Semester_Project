# Hybrid File Encryption & Decryption System (Android)

## Overview
This project is an Android application developed in Kotlin that provides secure file encryption and decryption. It uses AES-256 encryption and SHA-512 hashing to ensure confidentiality and data integrity, along with basic malware detection.

---

## Problem
Sensitive files on mobile devices are often stored or shared without proper security, making them vulnerable to:
- Unauthorized access
- Data tampering
- Malware threats

---

## Objectives
- Implement secure file encryption/decryption
- Use AES-256 (CBC mode with PKCS5Padding)
- Ensure integrity using SHA-512 hashing
- Compare AES vs DES performance
- Detect unsafe files via malware scanning
- Provide a user-friendly interface

---

## Technologies Used
- **Language:** Kotlin  
- **Platform:** Android  
- **IDE:** Android Studio  
- **Encryption:** AES-256  
- **Hashing:** SHA-512  
- **Comparison:** DES  
- **Storage:** SharedPreferences  

---

## Core Algorithms

### AES (Advanced Encryption Standard)
- 256-bit key encryption
- CBC mode for enhanced security
- PKCS5Padding for proper block handling

### DES (Data Encryption Standard)
- Used only for comparison
- Considered insecure due to small key size

### SHA-512
- Generates unique hash for each file
- Detects any modification in data

---

## System Workflow
1. **User Authentication**
   - Password setup and verification

2. **Encryption**
   - File selection → Malware check  
   - SHA-512 hash generation  
   - AES encryption with random IV  

3. **Decryption**
   - Hash verification  
   - If unchanged → decrypt  
   - If modified → block process  

4. **Performance Comparison**
   - Measures AES vs DES execution time  

---

## Key Features
- AES-256 secure encryption  
- SHA-512 integrity verification  
- Tamper detection  
- Random IV generation  
- Malware file scanning  
- AES vs DES comparison  
- Simple UI  

---

## Architecture
- Modular design
- Separate components for:
  - Encryption
  - Hashing
  - Malware scanning
  - Performance comparison

---

## Advantages
- Strong data security  
- Integrity verification  
- Tamper detection  
- Blocks risky files  
- Easy to use  

---

## Limitations
- No cloud storage support  
- DES used only for learning  
- Basic malware detection (extension/MIME only)  

---

## Future Improvements
- Cloud integration  
- Biometric authentication  
- AI-based malware detection  
- Secure file sharing  
- Hybrid encryption (RSA/ECC)  

---

## Conclusion
The system effectively secures files using AES-256 and ensures integrity with SHA-512. It demonstrates practical encryption techniques and highlights AES superiority over DES, providing a reliable mobile security solution.

---

## Links
- GitHub: https://github.com/haseebulhasan906/IS_Semester_Project  
- Medium: https://medium.com/@alihassancht4/building-a-hybrid-file-encryption-decryptionandroid-app-using-aes-and-sha-512-84e089caeb13  
