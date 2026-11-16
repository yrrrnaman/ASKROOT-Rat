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

## Security

Outbound TLS only. Device credentials are HMAC-signed. Commands come from a
fixed registry with no shell. Paths are jailed. Every action is audited.

MIT
