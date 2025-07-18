# Monorepo Project

This repository contains a monorepo with the following projects:
*   **taskflow-2025:** A frontend application built with React, TypeScript, and Vite.
*   **todo:** A backend application built with Java.

Further details about each project and how to run them can be found below.

## Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [Features](#features)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)
- [Acknowledgements](#acknowledgements)

## Installation

To get this project up and running, you'll need to install its components.

### Prerequisites

*   Node.js (for taskflow-2025) - You can download it from [https://nodejs.org/](https://nodejs.org/)
*   NPM (usually comes with Node.js) or Yarn (optional, for taskflow-2025)
*   Java Development Kit (JDK) version 17 or later (for the todo project) - You can download it from [Oracle JDK](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html) or use an alternative like OpenJDK.

### 1. taskflow-2025 (Frontend)

1.  Navigate to the `taskflow-2025` directory:
    ```bash
    cd taskflow-2025
    ```
2.  Install the dependencies:
    ```bash
    npm install
    ```
    Alternatively, if you prefer using Yarn:
    ```bash
    yarn install
    ```

### 2. todo (Backend)

The `todo` project is built using Java and Spring Boot with Gradle.

1.  Ensure you have JDK 17 installed and configured on your system.
2.  Navigate to the `todo` directory:
    ```bash
    cd todo
    ```
3.  The project uses the Gradle wrapper. Dependencies will be downloaded automatically when you build or run the project. No separate installation command for dependencies is typically needed. For example, you can build the project using:
    ```bash
    ./gradlew build 
    ```
    (On Windows, use `gradlew.bat build`)

## Usage

### 1. taskflow-2025 (Frontend)

1.  Navigate to the `taskflow-2025` directory:
    ```bash
    cd taskflow-2025
    ```
2.  To start the development server:
    ```bash
    npm run dev
    ```
    This will typically open the application in your default web browser (e.g., at `http://localhost:5173` or a similar address displayed in the console).

3.  To build the application for production:
    ```bash
    npm run build
    ```
    The production files will usually be placed in a `dist` folder.

4.  To preview the production build locally:
    ```bash
    npm run preview
    ```

### 2. todo (Backend)

1.  Navigate to the `todo` directory:
    ```bash
    cd todo
    ```
2.  To run the Spring Boot application:
    ```bash
    ./gradlew bootRun
    ```
    (On Windows, use `gradlew.bat bootRun`)
    The backend API will then be accessible, typically on `http://localhost:8080` (this can vary based on the application's configuration). Check the console output for the exact address and port.

    The API documentation is available via Swagger UI, usually at `http://localhost:8080/swagger-ui.html` once the application is running. (I saw an `API_DOCUMENTATION.md` and `springdoc-openapi-starter-webmvc-ui` in the gradle file, so this is a safe assumption).

## Features

This project combines a frontend application (`taskflow-2025`) and a backend API (`todo`) to deliver a complete solution.

### taskflow-2025 (Frontend)

*   **Modern Web Interface:** Built with React, TypeScript, and Vite for a fast and responsive user experience.
*   **Component-Based Architecture:** Organized into reusable React components (e.g., for authentication, dashboards, lists, tasks).
*   **Task Management UI:** Allows users to interact with lists and tasks (based on component names like `ListCard`, `TaskDialog`).
*   **User Authentication:** Includes an authentication page (`AuthPage.tsx`).
*   **Client-Side Routing:** Uses `react-router-dom` for seamless navigation within the application.
*   **Styling:** Utilizes Material-UI (`@mui/material`) for UI components.

### todo (Backend - API)

*   **RESTful API:** Provides endpoints for managing data (likely tasks, lists, users).
*   **Spring Boot Framework:** Robust and scalable backend built with Spring Boot.
*   **Data Persistence:** Uses Spring Data JPA for database interactions (e.g., storing tasks, user information).
*   **Security:** Implements security features using Spring Security, including JWT-based authentication.
*   **API Documentation:** Integrated Swagger UI for easy exploration and testing of API endpoints.
*   **Built with Java 17:** Utilizes modern Java features.

## Contributing

Contributions are welcome! If you'd like to contribute to this project, please follow these guidelines:

1.  **Fork the repository.**
2.  **Create a new branch** for your feature or bug fix:
    ```bash
    git checkout -b feature/your-feature-name
    ```
    or
    ```bash
    git checkout -b fix/your-bug-fix-name
    ```
3.  **Make your changes** and commit them with clear and descriptive messages.
4.  **Ensure your code lints** (for the frontend, you can use `npm run lint` in the `taskflow-2025` directory).
5.  **Push your changes** to your forked repository.
6.  **Open a Pull Request** to the main repository's `main` branch (or the relevant development branch).

### Reporting Bugs

If you find a bug, please open an issue on the GitHub repository. Include the following information:
*   A clear and descriptive title.
*   Steps to reproduce the bug.
*   Expected behavior.
*   Actual behavior.
*   Screenshots or error messages, if applicable.
*   Your environment (e.g., browser version, Node.js version, OS).

### Suggesting Enhancements

If you have an idea for an enhancement, please open an issue on the GitHub repository. Describe your idea clearly and explain why it would be beneficial.

## License

This project is licensed under the MIT License.

You can find the full license text in the `LICENSE` file if one is included in the repository. If not, this project is typically distributed under the terms of the MIT license. Please replace this section with your chosen license if different.

Example:
```
This project is licensed under the MIT License - see the LICENSE.md file for details.
```
Or, if you don't have a separate LICENSE file:
```
Copyright (c) [Year] [Your Name/Organization]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Contact

If you have any questions, feedback, or issues, please feel free to:
*   Open an issue on the [GitHub Issues page](https://github.com/your-username/your-repository/issues) (Please replace with the actual link to your repository issues page).
*   Contact the project maintainer(s) at `your-email@example.com` (Please replace with your actual contact email).
