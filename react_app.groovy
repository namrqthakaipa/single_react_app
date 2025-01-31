pipeline {
    agent any

    tools {
        nodejs 'NodeJS_16'
    }

    stages {
        
        // Stage 1: Git Checkout
        stage('Git Checkout') {
            steps {
                git url: "https://github.com/namrqthakaipa/single_react_app.git", branch: "main", credentialsId: "New_React_Token"
            }
        }

        // Stage 2: Install NPM Dependencies
        stage('NPM Install') {
            steps {
                sh "npm install"
            }
        }

        // Stage 3: Run Node Build
        stage('Node Build') {
            steps {
                sh "npm run build"
            }
        }

    // Stage 4: Install NPM Dependencies and Build
    stage('Clone Repository in EC2 ') {
    steps {
        script {
            def ec2_ip = 'ec2-65-1-110-154.ap-south-1.compute.amazonaws.com'
            def ec2_user = 'ubuntu'
            def key_path = '/var/lib/jenkins/Asia_key.pem'

          
            sh """
            ssh -i ${key_path} -o StrictHostKeyChecking=no -t ${ec2_user}@${ec2_ip} '
            
            sudo mkdir -p /opt/react_build
            sudo chown -R ${ec2_user}:${ec2_user} /opt/react_build
            
            if [ -d "/opt/single_react_app" ]; then
                sudo rm -rf /opt/single_react_app
            fi
            
            sudo git clone https://github.com/namrqthakaipa/single_react_app.git /opt/single_react_app
            '
            """
        }
    }
}

 // Stage 5: Configure Nginx
stage('Configure Nginx') {
    steps {
        script {
            def ec2_ip = 'ec2-65-1-110-154.ap-south-1.compute.amazonaws.com'
            def ec2_user = 'ubuntu'
            def key_path = '/var/lib/jenkins/Asia_key.pem'


            sh """
            ssh -i ${key_path} -o StrictHostKeyChecking=no -t ${ec2_user}@${ec2_ip} '
            sudo bash -c "echo \\"server {
                listen 80;
                server_name 65.1.110.154;
                            
                root /opt/react_build;
                index index.html;
                            
                location / {
                    try_files \\\\\\\$uri /index.html;
                }
                            
                error_page  404 /404.html;
                location = /40x.html {
                    root /usr/share/nginx/html;
                }
            }\\" > /etc/nginx/sites-available/react_app.conf"

    
            sudo rm -f /etc/nginx/sites-enabled/react_app.conf


            sudo ln -s /etc/nginx/sites-available/react_app.conf /etc/nginx/sites-enabled/

            sudo nginx -t
            sudo systemctl restart nginx
            sudo systemctl status nginx
            '
            """
        }
    }
}


    }
}
