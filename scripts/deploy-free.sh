#!/bin/bash

# Free Deployment Script
# Deploys to Railway (Backend) + Vercel (Frontend) - 100% FREE!

set -e

echo "🚀 Starting FREE deployment..."
echo "   Backend: Railway (500 hours/month free)"
echo "   Frontend: Vercel (unlimited free tier)"

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "📦 Installing Railway CLI..."
    npm install -g @railway/cli
fi

# Check if Vercel CLI is installed
if ! command -v vercel &> /dev/null; then
    echo "📦 Installing Vercel CLI..."
    npm install -g vercel
fi

echo "🔧 Deploying Backend to Railway..."

# Deploy backend to Railway
cd backend

# Login to Railway (if not already logged in)
if ! railway whoami &> /dev/null; then
    echo "🔐 Please login to Railway..."
    railway login
fi

# Initialize Railway project (if not already initialized)
if [ ! -f "railway.json" ]; then
    echo "📋 Initializing Railway project..."
    railway init
fi

# Set environment variables
echo "🔑 Setting environment variables..."
railway variables set OPENAI_API_KEY="$OPENAI_API_KEY"
railway variables set OPENAI_MODEL="gpt-4"
railway variables set STABILITY_API_KEY="$STABILITY_API_KEY"
railway variables set SPRING_PROFILES_ACTIVE="prod"

# Deploy to Railway
echo "🚀 Deploying to Railway..."
railway up

# Get Railway URL
RAILWAY_URL=$(railway status | grep "Deployment URL" | awk '{print $3}')
echo "✅ Backend deployed at: $RAILWAY_URL"

cd ..

echo "🎨 Deploying Frontend to Vercel..."

# Deploy frontend to Vercel
cd frontend

# Update Vercel config with Railway URL
sed -i.bak "s|your-railway-backend-url.railway.app|${RAILWAY_URL#https://}|g" vercel.json

# Login to Vercel (if not already logged in)
if ! vercel whoami &> /dev/null; then
    echo "🔐 Please login to Vercel..."
    vercel login
fi

# Deploy to Vercel
echo "🚀 Deploying to Vercel..."
vercel --prod

# Get Vercel URL
VERCEL_URL=$(vercel ls | grep "ai-agent" | awk '{print $2}')
echo "✅ Frontend deployed at: $VERCEL_URL"

cd ..

echo "🎉 FREE deployment completed!"
echo ""
echo "🌐 Your application is now live:"
echo "   Frontend: $VERCEL_URL"
echo "   Backend:  $RAILWAY_URL"
echo ""
echo "💰 Cost: $0/month (100% FREE!)"
echo ""
echo "📝 Next steps:"
echo "   1. Test your application"
echo "   2. Set up custom domain (optional)"
echo "   3. Monitor usage in Railway/Vercel dashboards"
echo ""
echo "🔧 To update:"
echo "   Backend: cd backend && railway up"
echo "   Frontend: cd frontend && vercel --prod"
echo ""
echo "📊 Usage limits:"
echo "   Railway: 500 hours/month (enough for 24/7)"
echo "   Vercel: Unlimited (free tier)" 