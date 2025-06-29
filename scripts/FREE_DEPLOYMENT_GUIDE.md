# FREE Deployment Guide

Run your AI Agent application completely FREE! Here are the best options:

## 🆓 **100% Free Hosting Options**

### **1. Railway + Vercel (Recommended)**
- ✅ **Railway**: 500 hours/month free (backend)
- ✅ **Vercel**: Unlimited free tier (frontend)
- ✅ **Total Cost**: $0/month
- ✅ **Custom domains** included
- ✅ **SSL certificates** included

### **2. Railway + Netlify**
- ✅ **Railway**: 500 hours/month free (backend)
- ✅ **Netlify**: Unlimited free tier (frontend)
- ✅ **Total Cost**: $0/month

### **3. Render + Vercel**
- ✅ **Render**: 750 hours/month free (backend)
- ✅ **Vercel**: Unlimited free tier (frontend)
- ✅ **Total Cost**: $0/month

### **4. Railway Only (Full Stack)**
- ✅ **Railway**: 500 hours/month free
- ✅ **Both frontend and backend** on Railway
- ✅ **Total Cost**: $0/month

## 🚀 **Quick Start: Railway + Vercel**

### **Step 1: Install Tools**
```bash
# Install Railway CLI
npm install -g @railway/cli

# Install Vercel CLI
npm install -g vercel
```

### **Step 2: Set Environment Variables**
```bash
export OPENAI_API_KEY="your_openai_api_key"
export STABILITY_API_KEY="your_stability_api_key"
```

### **Step 3: Deploy**
```bash
./deploy-free.sh
```

## 📋 **Manual Deployment Steps**

### **Backend on Railway**

1. **Create Railway Account**
   - Go to https://railway.app
   - Sign up with GitHub

2. **Deploy Backend**
   ```bash
   cd backend
   railway login
   railway init
   railway up
   ```

3. **Set Environment Variables**
   ```bash
   railway variables set OPENAI_API_KEY="your_key"
   railway variables set OPENAI_MODEL="gpt-4"
   railway variables set STABILITY_API_KEY="your_key"
   ```

### **Frontend on Vercel**

1. **Create Vercel Account**
   - Go to https://vercel.com
   - Sign up with GitHub

2. **Deploy Frontend**
   ```bash
   cd frontend
   vercel login
   vercel --prod
   ```

3. **Update API URL**
   - Get your Railway URL
   - Update `vercel.json` with the Railway URL

## 💰 **Free Tier Limits**

### **Railway (Backend)**
- ✅ **500 hours/month** (enough for 24/7 usage)
- ✅ **1GB RAM** per service
- ✅ **Custom domains**
- ✅ **SSL certificates**
- ✅ **Database included**

### **Vercel (Frontend)**
- ✅ **Unlimited** deployments
- ✅ **100GB bandwidth/month**
- ✅ **Custom domains**
- ✅ **SSL certificates**
- ✅ **Edge functions**

### **Render (Alternative)**
- ✅ **750 hours/month** (more than Railway)
- ✅ **512MB RAM**
- ✅ **Custom domains**
- ✅ **SSL certificates**

## 🔧 **Alternative: Railway Only**

If you want everything on one platform:

### **Deploy Both on Railway**
```bash
# Backend
cd backend
railway up

# Frontend (static build)
cd frontend
npm run build
railway up --service frontend
```

## 📊 **Cost Comparison**

| Platform | Cost | Hours/Month | RAM | Pros |
|----------|------|-------------|-----|------|
| **Railway** | $0 | 500 | 1GB | Easy, includes DB |
| **Vercel** | $0 | Unlimited | - | Perfect for Next.js |
| **Render** | $0 | 750 | 512MB | More hours |
| **Netlify** | $0 | Unlimited | - | Great for static sites |

## 🎯 **Recommendation**

**Railway + Vercel** is the best combination because:

1. **Railway**: Perfect for Spring Boot backends
2. **Vercel**: Optimized for Next.js frontends
3. **Both free**: No cost at all
4. **Easy deployment**: Simple CLI tools
5. **Great performance**: CDN and edge functions

## 🚀 **Deployment Commands**

### **Option 1: Automated (Recommended)**
```bash
./deploy-free.sh
```

### **Option 2: Manual**
```bash
# Backend
cd backend
railway up

# Frontend
cd frontend
vercel --prod
```

## 🔍 **Monitoring & Management**

### **Railway Dashboard**
- Monitor usage: https://railway.app/dashboard
- View logs: `railway logs`
- Check status: `railway status`

### **Vercel Dashboard**
- Monitor usage: https://vercel.com/dashboard
- View analytics: Built-in analytics
- Check deployments: `vercel ls`

## 🆘 **Troubleshooting**

### **Railway Issues**
```bash
# Check logs
railway logs

# Restart service
railway service restart

# Check status
railway status
```

### **Vercel Issues**
```bash
# Check deployments
vercel ls

# View logs
vercel logs

# Redeploy
vercel --prod
```

### **Common Problems**

1. **Environment Variables**
   - Make sure API keys are set in Railway
   - Check variable names match your code

2. **CORS Issues**
   - Update CORS settings with your Vercel domain
   - Check `application-prod.properties`

3. **Build Failures**
   - Check Java version (Railway supports Java 17)
   - Verify Maven dependencies

## 📈 **Scaling Up (If Needed)**

### **Railway Paid Plans**
- **$5/month**: 1000 hours, 2GB RAM
- **$20/month**: Unlimited hours, 8GB RAM

### **Vercel Paid Plans**
- **$20/month**: Pro plan with more features
- **$40/month**: Enterprise features

## 🎉 **Success!**

Once deployed, your application will be:
- ✅ **100% FREE** to run
- ✅ **Always available** (within free tier limits)
- ✅ **Automatically scaled**
- ✅ **SSL secured**
- ✅ **Custom domain ready**

Your AI Agent is now running in the cloud for FREE! 🚀 