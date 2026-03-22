# Salat Time — Mosque Admin Dashboard

## Project Overview

Salat Time is an admin dashboard for mosque managers to administer prayer (salat) times, manage slides, and update mosque profile information. The web UI is built with Vite, React, TypeScript, Tailwind CSS and shadcn/ui components. The project integrates with Supabase for authentication and data storage.

**Website**: https://salatetime.org/

## Local development

Prerequisites: Node.js and npm (or your preferred Node manager).

Quick start:

```sh
git clone <YOUR_GIT_URL>
cd <YOUR_PROJECT_NAME>
npm install
npm run dev
```

This will start the Vite dev server with hot reload (default port shown in console).

## Key Features

- Manage mosque prayer times per day and month
- Upsert daily prayer times (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha)
- Manage slides/images for mosque display
- Mosques and user access controlled via Supabase authentication

## Technology stack

- Vite
- React
- TypeScript
- Tailwind CSS
- shadcn/ui components
- Supabase (authentication + database)

## Deployment

Build for production:

```sh
npm run build
```

Deploy the `dist/` output to your hosting provider. For connecting a custom domain, configure the DNS records to point to your host and follow your hosting provider's instructions.

## Contributing

If you work on this repository locally, please open PRs for changes and include tests where appropriate. Follow the repository's linting and formatting rules.

If you need help running the project or updating content for Salat Time, open an issue describing the problem.
