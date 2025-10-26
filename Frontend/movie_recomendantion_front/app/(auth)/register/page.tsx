"use client";
import Image from "next/image";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";	
import { TbEye, TbEyeOff, TbMovie, TbCheck, TbX } from 'react-icons/tb';
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { FormEvent, useState } from "react";
import Link from "next/link";

export default function Register() {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordError, setPasswordError] = useState("");

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

  function handleSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();
    
    if (!validatePasswords(password, confirmPassword)) {
      return;
    }

    // Aqui você pode adicionar a lógica de registro
    console.log("Formulário válido, processando registro...");
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
              Seja Bem-Vindo(a)!
            </CardTitle>
            <CardDescription className="text-slate-600 dark:text-slate-400">
              Crie sua conta para começar a usar o nosso sistema de recomendação de filmes!
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <form className="space-y-4" onSubmit={handleSubmit}>
              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input id="username" type="text" placeholder="username123" required />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" type="email" placeholder="seu@email.com" required />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="password">Senha</Label>
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
                <Label htmlFor="confpassword">Confirmar Senha</Label>
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
                    As senhas coincidem
                  </p>
                )}
              </div>
              
              <Button 
                type="submit" 
                className={`w-full transition-all duration-500 ${
                  passwordError 
                    ? 'bg-gray-400 cursor-not-allowed hover:bg-gray-400' 
                    : 'hover:cursor-pointer hover:bg-yellow-500 hover:text-white'
                }`}
                disabled={!!passwordError}
              >
                Criar Conta
              </Button>
            </form>
            
            <p className="text-center text-sm text-slate-600 dark:text-slate-400">
              Já tem uma conta? {" "}
              <Button variant="link" className="text-blue-600 hover:cursor-pointer hover:text-blue-700 p-0 h-auto font-medium">
                <Link href="/login">Entre aqui</Link>
              </Button>
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}