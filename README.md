<img width="1024" height="1024" alt="image" src="https://github.com/user-attachments/assets/7573dd5c-3b76-4930-aa56-e508a1faf244" />
# AskRoot

One command. Complete control.

Remote device administration for machines you own. Enroll a host, approve it,
run validated commands, move files, keep an audit trail.

## Stack

Java 21 · Spring Boot 3 · PostgreSQL · Redis · React · TypeScript · Docker

## Run

    cp .env.example .env
    docker compose up -d --build
    open http://localhost:8080

Login: `admin@askroot.io` / `askroot-demo`

## Layout

    backend/      Spring Boot API + agent socket
    frontend/     React console
    device-agent/ outbound-only agent
    docker/       nginx edge
    database/     migrations


    AskRoot/
    ├── frontend/ │   ├── src/{components,pages,layouts,hooks,services,lib}
    │   ├── index.html · vite.config.ts · tailwind.config.ts · package.json
    ├── backend/
    │   ├── src/main/java/com/askroot/{config,controller,service,repository,entity,dto,security,websocket,device,terminal,filemanager,monitoring,audit,exception}
    │   ├── src/main/resources/{application.yml,db/migration}
    │   ├── src/test/java/com/askroot
    │   └── pom.xml · Dockerfile
    ├── device-agent/
    │   ├── src/main/java/com/askroot/agent/{enroll,runtime,exec,transfer,transport}
    │   └── pom.xml · Dockerfile
    ├── database/migrations/  → symlinked into backend resources
    ├── docker/{nginx.conf,postgres-init.sql}
    ├── .github/workflows/ci.yml
    ├── docker-compose.yml · .env.example · README.md · LICENSE · .gitignore

## Security

Outbound TLS only. Device credentials are HMAC-signed. Commands come from a
fixed registry with no shell. Paths are jailed. Every action is audited.

MIT
