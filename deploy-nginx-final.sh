#!/bin/bash
# Deploy Nginx Configuration for Supabase Dev (Updated with Kong Gateway ports)

set -e

echo "=========================================="
echo "  Supabase Dev Nginx Configuration"
echo "  Updated with Kong Gateway (port 54321)"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "❌ Please run as root (use sudo)"
    exit 1
fi

# Create nginx config directory if not exists
NGINX_SITES="/etc/nginx/sites-available"
NGINX_ENABLED="/etc/nginx/sites-enabled"
mkdir -p "$NGINX_SITES"
mkdir -p "$NGINX_ENABLED"

# Backup existing config if exists
if [ -f "$NGINX_SITES/supabase-dev.dailyworkerhub.com.conf" ]; then
    echo "📦 Backing up existing config..."
    cp "$NGINX_SITES/supabase-dev.dailyworkerhub.com.conf" \
       "$NGINX_SITES/supabase-dev.dailyworkerhub.com.conf.backup.$(date +%Y%m%d_%H%M%S)"
fi

# Copy new config
echo "📝 Installing new nginx configuration..."
cat << 'EOF' > "$NGINX_SITES/supabase-dev.dailyworkerhub.com.conf"
# Nginx Reverse Proxy Configuration for Supabase Dev
# All endpoints go through Kong Gateway at port 54321

server {
    listen 80;
    listen [::]:80;
    server_name supabase-dev.dailyworkerhub.com;

    # Enable logging for debugging
    access_log /var/log/nginx/supabase-dev-access.log;
    error_log /var/log/nginx/supabase-dev-error.log;

    # Supabase Auth - Via Kong Gateway port 54321
    location /auth/v1/ {
        proxy_pass http://127.0.0.1:54321/auth/v1/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
        proxy_send_timeout 300s;
    }

    # Supabase REST - Via Kong Gateway port 54321
    location /rest/v1/ {
        proxy_pass http://127.0.0.1:54321/rest/v1/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
        proxy_send_timeout 300s;
    }

    # Supabase Storage - Via Kong Gateway port 54321
    location /storage/v1/ {
        proxy_pass http://127.0.0.1:54321/storage/v1/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
        proxy_send_timeout 300s;

        # Handle large file uploads (max 100MB)
        client_max_body_size 100M;
    }

    # Supabase Functions - Via Kong Gateway port 54321
    location /functions/v1/ {
        proxy_pass http://127.0.0.1:54321/functions/v1/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
        proxy_send_timeout 300s;
    }

    # Supabase GraphQL - Via Kong Gateway port 54321
    location /graphql/v1/ {
        proxy_pass http://127.0.0.1:54321/graphql/v1/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
        proxy_send_timeout 300s;
    }

    # Studio - Direct to port 54323
    location /studio/ {
        proxy_pass http://127.0.0.1:54323/studio/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Root endpoint (health check)
    location / {
        return 200 '{"status":"ok","services":["auth","rest","storage","functions","graphql","studio"]}';
        add_header Content-Type application/json;
    }
}
EOF

# Enable the site
echo "🔗 Enabling site..."
ln -sf "$NGINX_SITES/supabase-dev.dailyworkerhub.com.conf" \
         "$NGINX_ENABLED/supabase-dev.dailyworkerhub.com.conf"

# Remove default site if exists
if [ -f "$NGINX_ENABLED/default" ]; then
    echo "🗑️  Removing default site..."
    rm -f "$NGINX_ENABLED/default"
fi

# Test nginx configuration
echo "🧪 Testing nginx configuration..."
nginx -t

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Nginx configuration is valid!"
    echo ""

    # Reload nginx
    echo "🔄 Reloading nginx..."
    systemctl reload nginx

    if [ $? -eq 0 ]; then
        echo "✅ Nginx reloaded successfully!"
        echo ""
        echo "=========================================="
        echo "  ✨ Configuration Complete!"
        echo "=========================================="
        echo ""
        echo "Available endpoints:"
        echo "  • Auth:    http://supabase-dev.dailyworkerhub.com/auth/v1/"
        echo "  • REST:    http://supabase-dev.dailyworkerhub.com/rest/v1/"
        echo "  • Storage: http://supabase-dev.dailyworkerhub.com/storage/v1/"
        echo "  • Funcs:   http://supabase-dev.dailyworkerhub.com/functions/v1/"
        echo "  • GraphQL: http://supabase-dev.dailyworkerhub.com/graphql/v1/"
        echo "  • Studio:  http://supabase-dev.dailyworkerhub.com/studio/"
        echo ""
        echo "Test with:"
        echo "  curl http://supabase-dev.dailyworkerhub.com/auth/v1/"
        echo "  curl http://supabase-dev.dailyworkerhub.com/rest/v1/"
        echo ""
    else
        echo "❌ Failed to reload nginx!"
        exit 1
    fi
else
    echo ""
    echo "❌ Nginx configuration test failed!"
    echo "Please check the configuration."
    exit 1
fi
