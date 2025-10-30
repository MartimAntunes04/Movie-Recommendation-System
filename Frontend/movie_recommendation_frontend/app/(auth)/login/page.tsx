"use client";
import Image from "next/image";
import { Card, CardContent, CardHeader, CardTitle,  CardDescription } from "@/components/ui/card";	
import { TbEye, TbEyeOff, TbMovie } from 'react-icons/tb';
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { login } from "@/Services/API";

export default function Login() {
  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const router = useRouter();
  const [status, setStatus] = useState<"idle" | "success" | "error">("idle");
  const [message, setMessage] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>){
   event.preventDefault();

   const data = await login(email,password);
   if(data.success){
      localStorage.setItem("token",data.token);
      router.push("/");
   }else {
      setStatus("error");
      setMessage(data.message || "Email ou password incorretos!");
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center ">
      <div className="w-full max-w-md space-y-8 p-8">
        <Card className="shadow-xl border-0 bg-white/80 backdrop-blur-sm dark:bg-slate-800/80">
          <CardHeader className="space-y-2 text-center">
          <div className="flex items-center justify-center gap-3 text-yellow-500 mb-4">
            <TbMovie className="text-4xl" />
            <span className="text-3xl font-bold bg-linear-to-r from-yellow-500 to-orange-500 bg-clip-text text-transparent">
              MovieRec
            </span>
          </div>
            <CardTitle className="text-2xl font-bold text-slate-900 dark:text-white">
              Welcome back!
            </CardTitle>
            <CardDescription className="text-slate-600 dark:text-slate-400">
              Login to your account
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input 
              id="email"
              type="email" 
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="seu@email.com" 
              required 
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <div className="relative group">
                <Input 
                id="password" 
                type={showPassword ? "text" : "password"} 
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="********" className="pr-10" 
                required 
                />
                <Button type="button" variant="ghost" className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-500 hover:text-yellow-400  group-hover:text-yellow-500 transition-all duration-500 hover:cursor-pointer" onClick={() => setShowPassword(!showPassword)}>
                {showPassword ? <TbEyeOff className="size-4" /> : <TbEye className="size-4" />}
              </Button>
              </div>
            </div>
            <Button type="submit" className="w-full hover:cursor-pointer hover:bg-yellow-500 hover:text-white transition-all duration-500">
              Entrar
            </Button>
             {status !== "idle" && (
          <p
            className={`mt-4 text-center font-bold ${
              status === "success" ? "text-green-600" : "text-red-600"
            }`}
          >
            {message}
          </p>
        )}
          </form>
          <p className="text-center text-sm text-slate-600 dark:text-slate-400">
              Don't have an account? {" "}
              <Button variant="link" className="text-blue-600 hover:cursor-pointer hover:text-blue-700 p-0 h-auto font-medium">
                <Link href="/register">Register</Link>
              </Button>
            </p>
          </CardContent>
          </Card>
      </div>
    </div>
  );
}
