-- Signalix Supabase schema + RLS policies

-- USERS
create table if not exists public.users (
  id uuid primary key default gen_random_uuid(),
  username text unique not null,
  token text not null,
  bio text default 'Busy',
  created_at timestamptz default now()
);

-- CONTACTS (both directions stored)
create table if not exists public.contacts (
  id uuid primary key default gen_random_uuid(),
  owner text not null,
  contact text not null,
  created_at timestamptz default now(),
  unique(owner, contact)
);

-- REQUESTS
create table if not exists public.requests (
  id uuid primary key default gen_random_uuid(),
  sender text not null,
  receiver text not null,
  status text not null default 'pending',
  created_at timestamptz default now()
);

-- BLOCKED
create table if not exists public.blocked (
  id uuid primary key default gen_random_uuid(),
  owner text not null,
  blocked text not null,
  created_at timestamptz default now(),
  unique(owner, blocked)
);

-- SESSIONS (optional)
create table if not exists public.sessions (
  id uuid primary key default gen_random_uuid(),
  username text not null,
  token text not null,
  device_id text not null,
  created_at timestamptz default now(),
  updated_at timestamptz default now(),
  unique(username, device_id)
);

-- RLS
alter table public.users enable row level security;
alter table public.contacts enable row level security;
alter table public.requests enable row level security;
alter table public.blocked enable row level security;
alter table public.sessions enable row level security;

-- Users: allow read by anyone with anon key; allow insert/update by anon (token-based app)
create policy if not exists "users_read" on public.users
  for select using (true);
create policy if not exists "users_write" on public.users
  for insert with check (true);
create policy if not exists "users_update" on public.users
  for update using (true);

-- Contacts: allow read/write
create policy if not exists "contacts_rw" on public.contacts
  for all using (true) with check (true);

-- Requests: allow read/write
create policy if not exists "requests_rw" on public.requests
  for all using (true) with check (true);

-- Blocked: allow read/write
create policy if not exists "blocked_rw" on public.blocked
  for all using (true) with check (true);

-- Sessions: allow read/write
create policy if not exists "sessions_rw" on public.sessions
  for all using (true) with check (true);
