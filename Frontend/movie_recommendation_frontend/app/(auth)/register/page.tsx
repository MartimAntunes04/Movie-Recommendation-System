"use client";
import Image from "next/image";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";	
import { TbEye, TbEyeOff, TbMovie, TbCheck, TbX } from 'react-icons/tb';
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function Register() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [username, setUsername] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName,setLastName] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [backendError, setBackendError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();


  const validatePasswords = (pass: string, confirmPass: string) => {
    if (confirmPass && pass !== confirmPass) {
      setPasswordError("As senhas não coincidem");
      return false;
    } else {
      setPasswordError("");
      return true;
    }
  };

  const handlePasswordChange = (value: string) => {
    setPassword(value);
    validatePasswords(value, confirmPassword);
  };

  const handleConfirmPasswordChange = (value: string) => {
    setConfirmPassword(value);
    validatePasswords(password, value);
  };

  const isFormInvalid = !!passwordError || !email || !password || !username || !firstName || !lastName || !confirmPassword;

  async function handleSubmit(event: FormEvent<HTMLFormElement>){
    event.preventDefault();
    
    if (!validatePasswords(password, confirmPassword)) {
      return;
    }

     try {
    const response = await fetch("http://localhost:8080/signup", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username,
        email,
        password,
        firstName,
        lastName,
      }),
    });

    const data = await response.json();

    if (data.success) {

      setBackendError(""); // limpar erros
      setSuccessMessage(data.message || "Conta criada com sucesso!");


      setEmail(""); 
      setPassword(""); 
      setConfirmPassword("");
      setUsername(""); 
      setFirstName(""); 
      setLastName("");

       setTimeout(() => {
        router.push("/login");
      }, 1500);
     
    } else {
       setBackendError(data.message);
    }
  } catch (error) {
    setBackendError("Ocorreu um erro a tentar criar conta.");
  }finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center ">
      <div className="w-full max-w-md space-y-8 p-8">
        <Card className="shadow-xl border-0 bg-white/80 backdrop-blur-sm dark:bg-slate-800/80">
          <CardHeader className="space-y-2 text-center">
            <div className="flex items-center justify-center gap-3 text-yellow-500 mb-4">
              <TbMovie className="text-4xl" />
              <span className="text-3xl font-bold bg-gradient-to-r from-yellow-500 to-orange-500 bg-clip-text text-transparent">
                MovieRec
              </span>
            </div>
            <CardTitle className="text-2xl font-bold text-slate-900 dark:text-white">
              Welcome!
            </CardTitle>
            <CardDescription className="text-slate-600 dark:text-slate-400">
              Create your account to start using our movie recommendation system!
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="flex gap-4">
             <div className="flex-1 space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input 
                id="firstName" 
                type="firstName"
                autoComplete="given-name"
                value={firstName}
                onChange={e=>setFirstName(e.target.value)} 
                placeholder="Martim" 
                required />
              </div>

              <div className="flex-1 space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input 
                id="lastName" 
                type="text" 
                autoComplete="family-name"
                value={lastName}
                onChange={e=>setLastName(e.target.value)} 
                placeholder="Antunes" 
                required />
              </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input 
                id="username" 
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="Martim123" 
                required />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input 
                id="email" 
                type="email"
                autoComplete="email"
                value={email}
                onChange={e=>setEmail(e.target.value)} 
                placeholder="martim@gmail.com" 
                required />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <div className="relative group">
                  <Input 
                    id="password" 
                    type={showPassword ? "text" : "password"} 
                    placeholder="********" 
                    className="pr-10" 
                    required 
                    value={password}
                    onChange={(e) => handlePasswordChange(e.target.value)}
                  />
                  <Button 
                    type="button" 
                    variant="ghost" 
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-500 hover:text-yellow-400 group-hover:text-yellow-500 transition-all duration-500 hover:cursor-pointer" 
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? <TbEyeOff className="size-4" /> : <TbEye className="size-4" />}
                  </Button>
                </div>
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="confpassword">Confirm Password</Label>
                <div className="relative group">
                  <Input 
                    id="confpassword" 
                    type={showConfirmPassword ? "text" : "password"} 
                    placeholder="********" 
                    className={`pr-10 ${passwordError ? 'border-red-500 focus:border-red-500' : confirmPassword && password === confirmPassword ? 'border-green-500 focus:border-green-500' : ''}`}
                    required 
                    value={confirmPassword}
                    onChange={(e) => handleConfirmPasswordChange(e.target.value)}
                  />
                  <Button 
                    type="button" 
                    variant="ghost" 
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-500 hover:text-yellow-400 group-hover:text-yellow-500 transition-all duration-500 hover:cursor-pointer" 
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  >
                    {showConfirmPassword ? <TbEyeOff className="size-4" /> : <TbEye className="size-4" />}
                  </Button>
                  
                  {/* Ícone de validação */}
                  {confirmPassword && (
                    <div className="absolute right-10 top-1/2 -translate-y-1/2">
                      {password === confirmPassword ? (
                        <TbCheck className="size-4 text-green-500" />
                      ) : (
                        <TbX className="size-4 text-red-500" />
                      )}
                    </div>
                  )}
                </div>
                
                {/* Mensagem de erro */}
                {passwordError && (
                  <p className="text-sm text-red-500 flex items-center gap-1">
                    <TbX className="size-3" />
                    {passwordError}
                  </p>
                )}
                
                {/* Mensagem de sucesso */}
                {confirmPassword && password === confirmPassword && !passwordError && (
                  <p className="text-sm text-green-500 flex items-center gap-1">
                    <TbCheck className="size-3" />
                    The passwords match
                  </p>
                )}

                {/* Backend Error */}
                {backendError && (
                  <p className="text-sm text-red-500 flex items-center gap-1 mt-2">
                    <TbX className="size-3" /> {backendError}
                  </p>
)}

                {successMessage && (
                  <p className="text-sm text-green-500 flex items-center gap-1 mt-2 animate-fade-in">
                    <TbCheck className="size-3" /> {successMessage}
                  </p>
)}

                
              </div>
              
              <Button
                type="submit"
                className={`w-full transition-all duration-500 ${isFormInvalid || loading ? 'bg-gray-400 cursor-not-allowed' : 'hover:bg-yellow-500 hover:text-white'}`}
                disabled={isFormInvalid || loading}
              >
                {loading ? "To be created..." : "Create Account"}
              </Button>
            </form>
            
            <p className="text-center text-sm text-slate-600 dark:text-slate-400">    
              Do you already have an account? {" "}
              <Button variant="link" className="text-blue-600 hover:cursor-pointer hover:text-blue-700 p-0 h-auto font-medium">
                <Link href="/login">Click here</Link>
              </Button>
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}